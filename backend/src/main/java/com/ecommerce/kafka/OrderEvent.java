package com.ecommerce.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kafka event payload for order events.
 * Serialized as JSON and published to the order-events topic.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent implements Serializable {

    private Long orderId;
    private Long userId;
    private String userEmail;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}
