package com.citse.kora.config;

import com.citse.kunduApp.avro.UserInvitationEvent;
import com.citse.kora.properties.KafkaValues;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

@Singleton
public class KafkaConfig {

    @Inject
    KafkaValues kafkaValues;

    @Produces
    @Singleton
    @Named("consumer")
    public Consumer<String, UserInvitationEvent> createConsumer() {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaValues.bootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());

        props.put(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                kafkaValues.schema().registry().url()
        );

        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "kora-api-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaValues.consumer().groupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaValues.consumer().autoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaValues.consumer().autoCommit());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaValues.consumer().maxPollRecords());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaValues.consumer().maxPollIntervalMs());

        // true: deserializa directamente a la clase generada UserInvitationEvent (no a GenericRecord)
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, kafkaValues.consumer().avroReader());

        return new KafkaConsumer<>(props);
    }
}
