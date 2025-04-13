package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListResponse {
    private Integer productId;
    private Integer sellerId;
    private Integer categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private String status;
}