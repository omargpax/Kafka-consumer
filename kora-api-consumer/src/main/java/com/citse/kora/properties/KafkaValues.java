package com.citse.kora.properties;

import io.smallrye.config.ConfigMapping;

/** Valores de Kafka (application.kafka-values.* en application.yml). Mismo patron que api-consumer-poc-05. */
@ConfigMapping(prefix = "application.kafka-values")
public interface KafkaValues {

    String topic();

    String bootstrapServers();

    Schema schema();

    Consumer consumer();

    interface Schema {

        Registry registry();

        interface Registry {

            String url();
        }
    }

    interface Consumer {

        String groupId();

        int maxPollRecords();

        int maxPollIntervalMs();

        boolean avroReader();

        String autoOffsetReset();

        boolean autoCommit();

        /** Espera (ms) antes de reintentar cuando falla el procesamiento de un registro. */
        long retryBackoffMs();
    }
}
