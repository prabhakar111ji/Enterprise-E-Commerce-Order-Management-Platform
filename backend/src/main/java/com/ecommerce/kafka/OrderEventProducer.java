package com.ecommerce.kafka;

import com.ecommerce.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka Producer — publishes order events to the order-events topic.
 *
 * Called by OrderService after a successful order creation.
 * The message is sent asynchronously — does not block the order response.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishOrderEvent(OrderEvent event) {
        log.info("Publishing order event for Order #{}", event.getOrderId());
        kafkaTemplate.send(KafkaConfig.ORDER_EVENTS_TOPIC, String.valueOf(event.getOrderId()), event);
    }
}
