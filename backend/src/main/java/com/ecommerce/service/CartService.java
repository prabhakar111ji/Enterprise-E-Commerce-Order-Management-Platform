package com.ecommerce.service;

import com.ecommerce.dto.cart.CartItemRequest;
import com.ecommerce.dto.cart.CartResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Cart Service — manages user shopping carts.
 *
 * Redis caching strategy:
 * - Cart is stored in PostgreSQL (source of truth)
 * - Redis caches the CartResponse for fast reads
 * - Cache is invalidated on every write operation
 *
 * WHY this approach?
 * - Cart is read on every page load → high read frequency
 * - Writes (add/update/remove) are less frequent
 * - Redis provides sub-millisecond reads vs PostgreSQL's ~5-10ms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    /** Get current user's cart. Cached in Redis. */
    @Cacheable(value = "carts", key = "#userId")
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId).orElse(null);
        return cartMapper.toResponse(cart);
    }

    /** Add item to cart. Evicts cart cache. */
    @Transactional
    @CacheEvict(value = "carts", key = "#userId")
    public CartResponse addItemToCart(Long userId, CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        Cart cart = getOrCreateCart(userId);

        // Check if product already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }

        log.info("Added product {} to cart for user {}", product.getName(), userId);

        // Return fresh cart
        Cart updatedCart = cartRepository.findByUserIdWithItems(userId).orElse(cart);
        return cartMapper.toResponse(updatedCart);
    }

    /** Update item quantity in cart. */
    @Transactional
    @CacheEvict(value = "carts", key = "#userId")
    public CartResponse updateCartItem(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not in cart"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        Cart updatedCart = cartRepository.findByUserIdWithItems(userId).orElse(cart);
        return cartMapper.toResponse(updatedCart);
    }

    /** Remove item from cart. */
    @Transactional
    @CacheEvict(value = "carts", key = "#userId")
    public CartResponse removeCartItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);

        Cart updatedCart = cartRepository.findByUserIdWithItems(userId).orElse(cart);
        return cartMapper.toResponse(updatedCart);
    }

    /** Clear entire cart. */
    @Transactional
    @CacheEvict(value = "carts", key = "#userId")
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
            log.info("Cart cleared for user {}", userId);
        }
    }

    /** Get or create a cart for a user. */
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }

    /** Get Cart entity for order processing (internal use). */
    public Cart getCartEntity(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));
    }
}
