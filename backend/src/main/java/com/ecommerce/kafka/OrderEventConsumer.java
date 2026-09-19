package com.ecommerce.kafka;

import com.ecommerce.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer — listens to order-events topic.
 *
 * In a real application, this would:
 * - Send email notifications
 * - Update analytics dashboards
 * - Trigger shipping workflows
 *
 * For this project, it logs the notification (simulated).
 */
@Slf4j
@Service
public class OrderEventConsumer {

    @KafkaListener(topics = KafkaConfig.ORDER_EVENTS_TOPIC, groupId = "ecommerce-group")
    public void handleOrderEvent(OrderEvent event) {
        log.info("=== ORDER NOTIFICATION ===");
        log.info("Order #{} {} successfully!", event.getOrderId(), event.getStatus());
        log.info("User: {} | Amount: ${}", event.getUserEmail(), event.getTotalAmount());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("==========================");

        // In production: send email, SMS, push notification, etc.
    }
}
