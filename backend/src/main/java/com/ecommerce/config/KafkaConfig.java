package com.ecommerce.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Configuration — creates topics programmatically.
 *
 * WHY Kafka?
 * - Asynchronous event processing — order confirmation doesn't block on notifications
 * - Decouples order service from notification logic
 * - Enables future consumers (email service, analytics) without changing producer
 *
 * ALTERNATIVE: Synchronous REST call to notification service.
 * TRADE-OFF: Kafka adds infrastructure complexity but provides reliability, replay, and decoupling.
 */
@Configuration
public class KafkaConfig {

    public static final String ORDER_EVENTS_TOPIC = "order-events";

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(ORDER_EVENTS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
