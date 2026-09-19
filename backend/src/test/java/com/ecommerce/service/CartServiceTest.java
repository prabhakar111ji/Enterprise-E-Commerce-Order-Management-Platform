package com.ecommerce.service;

import com.ecommerce.dto.cart.CartItemRequest;
import com.ecommerce.dto.cart.CartResponse;
import com.ecommerce.entity.*;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@test.com").build();
        product = Product.builder().id(1L).name("Test Product").price(new BigDecimal("50.00")).build();
        cart = Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
    }

    @Test
    @DisplayName("Should add item to cart")
    void addItemToCart_Success() {
        CartItemRequest request = new CartItemRequest(1L, 2);
        CartResponse expectedResponse = CartResponse.builder().cartId(1L).totalPrice(new BigDecimal("100.00")).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(CartItem.builder().id(1L).build());
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toResponse(any(Cart.class))).thenReturn(expectedResponse);

        CartResponse result = cartService.addItemToCart(1L, request);

        assertThat(result.getCartId()).isEqualTo(1L);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should get cart for user")
    void getCart_Success() {
        CartResponse expectedResponse = CartResponse.builder().cartId(1L).totalPrice(BigDecimal.ZERO).build();
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toResponse(cart)).thenReturn(expectedResponse);

        CartResponse result = cartService.getCart(1L);

        assertThat(result.getCartId()).isEqualTo(1L);
    }
}
