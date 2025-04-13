package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Product;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class ProductResponse {
    private Integer productId;
    private Integer sellerId;
    private Integer categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private String status;
    private Boolean isApproved;
    private Timestamp createdAt;
    private Timestamp listedAt;
    private Timestamp updatedAt;
}