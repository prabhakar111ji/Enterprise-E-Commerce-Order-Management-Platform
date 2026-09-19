package com.ecommerce.controller;

import com.ecommerce.dto.product.ProductResponse;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/products should return paginated products")
    void getProducts_ReturnsPage() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .id(1L).name("Test Product").price(new BigDecimal("99.99"))
                .category("Electronics").stock(50).build();

        Page<ProductResponse> page = new PageImpl<>(List.of(product));
        when(productService.getProducts(any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.content[0].price").value(99.99));
    }

    @Test
    @DisplayName("GET /api/products/{id} should return product details")
    void getProduct_ReturnsProduct() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .id(1L).name("MacBook Pro").price(new BigDecimal("2499.99"))
                .category("Electronics").stock(25).build();

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MacBook Pro"))
                .andExpect(jsonPath("$.stock").value(25));
    }
}
