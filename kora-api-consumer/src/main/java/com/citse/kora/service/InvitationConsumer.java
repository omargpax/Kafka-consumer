package com.citse.kora.service;

import com.citse.kunduApp.avro.UserInvitationEvent;
import com.citse.kora.properties.KafkaValues;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.jbosslog.JBossLog;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Consume user-invitation-topic con un KafkaConsumer propio (mismo patron que api-consumer-poc-05).
 *
 * Semantica "al menos una vez": el offset se confirma (commitSync) DESPUES de guardar en BD.
 * Si un evento llega repetido (reintento, reinicio, offset reset) no se duplica porque
 * InvitationService.registerIfNew comprueba usuarios e invitaciones por correo.
 */
@JBossLog
@ApplicationScoped
public class InvitationConsumer {

    @Inject
    KafkaValues kafkaValues;

    @Inject
    InvitationService invitationService;

    @Inject
    @Named("consumer")
    Consumer<String, UserInvitationEvent> consumer;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(r -> new Thread(r, "kora-invitation-consumer"));

    private volatile boolean running = true;

    void onStart(@Observes StartupEvent event) {
        log.infof("#### Init consume. topic=%s group=%s", kafkaValues.topic(), kafkaValues.consumer().groupId());
        executor.submit(this::pollLoop);
    }

    void onStop(@Observes ShutdownEvent event) {
        running = false;
        consumer.wakeup(); // unico metodo de KafkaConsumer seguro de invocar desde otro hilo
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void pollLoop() {
        try {
            consumer.subscribe(Collections.singleton(kafkaValues.topic()));
            while (running) {
                try {
                    ConsumerRecords<String, UserInvitationEvent> records = consumer.poll(Duration.ofSeconds(1));
                    if (records.isEmpty()) {
                        continue;
                    }
                    boolean failed = processBatch(records);
                    consumer.commitSync();
                    if (failed) {
                        pause();
                    }
                } catch (WakeupException e) {
                    if (!running) {
                        break;
                    }
                } catch (RecordDeserializationException e) {
                    // Mensaje que no se puede deserializar (esquema incompatible, basura): se omite para no bloquear el topic
                    log.errorf("Mensaje no deserializable (partition=%s, offset=%d), se omite: %s",
                            e.topicPartition(), e.offset(), e.getMessage());
                    consumer.seek(e.topicPartition(), e.offset() + 1);
                } catch (RuntimeException e) {
                    log.errorf("Error en el polling de Kafka: %s", e.toString());
                    pause();
                }
            }
        } finally {
            try {
                consumer.close();
            } catch (RuntimeException e) {
                log.warnf("Error al cerrar el consumer: %s", e.toString());
            }
            log.info("#### Consumer detenido");
        }
    }

    /**
     * Procesa los registros en orden. Si uno falla (por ejemplo la BD no responde), reposiciona el consumer en
     * ese offset para reintentarlo y devuelve true: lo ya procesado se confirma, lo pendiente se vuelve a leer.
     */
    private boolean processBatch(ConsumerRecords<String, UserInvitationEvent> records) {
        boolean failed = false;
        for (TopicPartition partition : records.partitions()) {
            List<ConsumerRecord<String, UserInvitationEvent>> partitionRecords = records.records(partition);
            if (failed) {
                consumer.seek(partition, partitionRecords.get(0).offset());
                continue;
            }
            for (ConsumerRecord<String, UserInvitationEvent> record : partitionRecords) {
                try {
                    if (record.value() == null) {
                        continue;
                    }
                    var outcome = invitationService.registerIfNew(record.value());
                    log.debugf("offset=%d partition=%d -> %s", record.offset(), record.partition(), outcome);
                } catch (RuntimeException e) {
                    log.errorf("Error al procesar el evento (partition=%d, offset=%d), se reintentara: %s",
                            record.partition(), record.offset(), e.toString());
                    consumer.seek(partition, record.offset());
                    failed = true;
                    break;
                }
            }
        }
        return failed;
    }

    private void pause() {
        try {
            Thread.sleep(kafkaValues.consumer().retryBackoffMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
