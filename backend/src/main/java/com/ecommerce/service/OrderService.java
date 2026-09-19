package com.ecommerce.service;

import com.ecommerce.dto.order.OrderResponse;
import com.ecommerce.dto.order.UpdateOrderStatusRequest;
import com.ecommerce.entity.*;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.kafka.OrderEvent;
import com.ecommerce.kafka.OrderEventProducer;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Service — handles the complete order lifecycle.
 *
 * The placeOrder() method is the most important business logic:
 * 1. Validate cart (not empty)
 * 2. Check inventory for ALL items
 * 3. Create order + order items
 * 4. Reduce stock for all items
 * 5. Simulate payment
 * 6. Mark order CONFIRMED
 * 7. Clear user's cart
 * 8. Publish Kafka event
 *
 * All steps 1-7 happen in a SINGLE TRANSACTION.
 * If any step fails, the entire transaction rolls back.
 *
 * WHY @Transactional?
 * - Ensures atomicity: either ALL changes happen or NONE
 * - Prevents partial orders (e.g., order created but stock not reduced)
 * - Automatic rollback on any RuntimeException
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;
    private final OrderMapper orderMapper;
    private final OrderEventProducer orderEventProducer;

    /**
     * Place a new order from the user's cart.
     * This is the core business transaction of the application.
     */
    @Transactional
    @CacheEvict(value = "carts", key = "#userId")
    public OrderResponse placeOrder(Long userId) {
        // 1. Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 2. Get cart with items
        Cart cart = cartService.getCartEntity(userId);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Add items before placing an order.");
        }

        // 3. Check inventory for all items BEFORE creating the order
        for (CartItem cartItem : cart.getItems()) {
            inventoryService.checkStock(
                    cartItem.getProduct().getId(),
                    cartItem.getProduct().getName(),
                    cartItem.getQuantity());
        }

        // 4. Calculate total amount
        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Create order
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.CREATED)
                .build();

        // 6. Create order items (capturing price at time of purchase)
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getProduct().getPrice())
                        .build())
                .collect(Collectors.toList());

        order.setItems(orderItems);

        // 7. Reduce inventory stock
        for (CartItem cartItem : cart.getItems()) {
            inventoryService.reduceStock(cartItem.getProduct().getId(), cartItem.getQuantity());
        }

        // 8. Simulate payment (always succeeds in this version)
        log.info("Simulating payment for order... Payment successful!");

        // 9. Mark order as CONFIRMED
        order.setStatus(OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);

        // 10. Clear user's cart
        cartService.clearCart(userId);

        log.info("Order #{} placed successfully for user {}", savedOrder.getId(), user.getEmail());

        // 11. Publish Kafka event (async — outside transaction scope for reliability)
        try {
            OrderEvent event = OrderEvent.builder()
                    .orderId(savedOrder.getId())
                    .userId(userId)
                    .userEmail(user.getEmail())
                    .status(savedOrder.getStatus().name())
                    .totalAmount(savedOrder.getTotalAmount())
                    .timestamp(LocalDateTime.now())
                    .build();
            orderEventProducer.publishOrderEvent(event);
        } catch (Exception e) {
            // Kafka failure should NOT rollback the order
            log.error("Failed to publish order event for Order #{}: {}", savedOrder.getId(), e.getMessage());
        }

        return orderMapper.toResponse(savedOrder);
    }

    /** Get all orders for a user. */
    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(orderMapper::toResponse);
    }

    /** Get order details by ID. Verifies ownership. */
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }

        return orderMapper.toResponse(order);
    }

    /** Cancel an order. Only CREATED or CONFIRMED orders can be cancelled. */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel order with status: " + order.getStatus());
        }

        // Restore inventory stock
        for (OrderItem item : order.getItems()) {
            inventoryService.restoreStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        log.info("Order #{} cancelled", orderId);
        return orderMapper.toResponse(savedOrder);
    }

    // === ADMIN METHODS ===

    /** Get all orders (admin). */
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(orderMapper::toResponse);
    }

    /** Update order status (admin). */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            order.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + request.getStatus());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order #{} status updated to {}", orderId, savedOrder.getStatus());
        return orderMapper.toResponse(savedOrder);
    }
}
