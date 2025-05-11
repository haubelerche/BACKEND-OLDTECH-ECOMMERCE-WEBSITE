package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;

import lombok.*;


import java.math.BigDecimal;
import java.sql.Timestamp;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;

@Entity
@Table(name = "product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer productId;

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer sellerId;

    @Column(columnDefinition = "INT UNSIGNED")
    private Integer categoryId;

    private String name;

    private String description;

    private BigDecimal price;

    private ProductStatusEnum status;

    private Boolean isApproved;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    @Builder.Default
    @Column(name = "is_visible")
    private Boolean isVisible = true; 
    
}