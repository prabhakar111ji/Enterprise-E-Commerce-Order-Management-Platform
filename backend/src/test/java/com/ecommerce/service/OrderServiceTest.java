package com.ecommerce.service;

import com.ecommerce.dto.order.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.kafka.OrderEventProducer;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private CartService cartService;
    @Mock private InventoryService inventoryService;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@test.com").role(Role.USER).build();
        product = Product.builder().id(1L).name("Test Product").price(new BigDecimal("100.00")).build();

        cart = Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();
        cart.getItems().add(cartItem);
    }

    @Test
    @DisplayName("Should place order successfully")
    void placeOrder_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartService.getCartEntity(1L)).thenReturn(cart);
        doNothing().when(inventoryService).checkStock(anyLong(), anyString(), anyInt());
        doNothing().when(inventoryService).reduceStock(anyLong(), anyInt());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderResponse mockResponse = OrderResponse.builder()
                .id(1L).status("CONFIRMED").totalAmount(new BigDecimal("200.00")).build();
        when(orderMapper.toResponse(any(Order.class))).thenReturn(mockResponse);

        OrderResponse result = orderService.placeOrder(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        verify(inventoryService).reduceStock(1L, 2);
        verify(cartService).clearCart(1L);
    }

    @Test
    @DisplayName("Should throw exception when cart is empty")
    void placeOrder_EmptyCart() {
        Cart emptyCart = Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartService.getCartEntity(1L)).thenReturn(emptyCart);

        assertThatThrownBy(() -> orderService.placeOrder(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    @DisplayName("Should cancel order and restore stock")
    void cancelOrder_Success() {
        Order order = Order.builder()
                .id(1L).user(user).status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("200.00"))
                .items(List.of(OrderItem.builder().product(product).quantity(2).price(new BigDecimal("100.00")).build()))
                .build();

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(
                OrderResponse.builder().id(1L).status("CANCELLED").build());

        OrderResponse result = orderService.cancelOrder(1L, 1L);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(inventoryService).restoreStock(1L, 2);
    }
}
