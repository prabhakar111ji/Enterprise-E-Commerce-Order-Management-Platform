package com.ecommerce.mapper;

import com.ecommerce.dto.product.ProductResponse;
import com.ecommerce.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        int stock = product.getInventory() != null ? product.getInventory().getQuantity() : 0;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .stock(stock)
                .build();
    }
}
