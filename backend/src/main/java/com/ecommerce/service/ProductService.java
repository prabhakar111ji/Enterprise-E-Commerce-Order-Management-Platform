package com.ecommerce.service;

import com.ecommerce.dto.product.ProductRequest;
import com.ecommerce.dto.product.ProductResponse;
import com.ecommerce.entity.Inventory;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductMapper productMapper;

    /** Get all products with optional filtering. Cached in Redis for fast reads. */
    // @Cacheable(value = "products", key = "'list-' + #category + '-' + #search + '-' + #pageable.pageNumber")
    public Page<ProductResponse> getProducts(String category, String search, Pageable pageable) {
        Page<Product> products = productRepository.findByFilters(category, search, pageable);
        return products.map(productMapper::toResponse);
    }

    /** Get single product by ID. Cached individually. */
    @Cacheable(value = "product", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return productMapper.toResponse(product);
    }

    /** Create new product with inventory. Admin only. */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .build();

        product = productRepository.save(product);

        Inventory inventory = Inventory.builder()
                .product(product)
                .quantity(request.getStock())
                .build();
        inventoryRepository.save(inventory);

        product.setInventory(inventory);
        log.info("Product created: {} (stock: {})", product.getName(), request.getStock());

        return productMapper.toResponse(product);
    }

    /** Update existing product. Admin only. */
    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());

        if (product.getInventory() != null) {
            product.getInventory().setQuantity(request.getStock());
        } else {
            Inventory inventory = Inventory.builder()
                    .product(product)
                    .quantity(request.getStock())
                    .build();
            inventoryRepository.save(inventory);
            product.setInventory(inventory);
        }

        product = productRepository.save(product);
        log.info("Product updated: {}", product.getName());
        return productMapper.toResponse(product);
    }

    /** Delete product. Admin only. */
    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        productRepository.delete(product);
        log.info("Product deleted: {}", product.getName());
    }
}
