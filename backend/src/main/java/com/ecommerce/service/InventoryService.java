package com.ecommerce.service;

import com.ecommerce.entity.Inventory;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /** Check if sufficient stock is available for a product. */
    public void checkStock(Long productId, String productName, int requestedQuantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productName));

        if (inventory.getQuantity() < requestedQuantity) {
            throw new InsufficientStockException(productName, requestedQuantity, inventory.getQuantity());
        }
    }

    /** Reduce stock after successful order. Must be called within a transaction. */
    @Transactional
    public void reduceStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + productId));

        int newQuantity = inventory.getQuantity() - quantity;
        if (newQuantity < 0) {
            throw new InsufficientStockException("Product ID " + productId, quantity, inventory.getQuantity());
        }

        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
        log.info("Stock reduced for product {}: {} -> {}", productId, inventory.getQuantity() + quantity, newQuantity);
    }

    /** Restore stock on order cancellation. */
    @Transactional
    public void restoreStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + productId));

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
        log.info("Stock restored for product {}: +{}", productId, quantity);
    }
}
