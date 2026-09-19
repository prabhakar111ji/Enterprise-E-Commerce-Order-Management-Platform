package com.ecommerce.entity.enums;

/**
 * Tracks the lifecycle of an order.
 * CREATED → CONFIRMED → SHIPPED → DELIVERED
 *                    or → CANCELLED
 */
public enum OrderStatus {
    CREATED,
    CONFIRMED,
    CANCELLED,
    SHIPPED,
    DELIVERED
}
