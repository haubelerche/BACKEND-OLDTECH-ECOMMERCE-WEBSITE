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
    @Column(name = "product_id", columnDefinition = "INT UNSIGNED", nullable = false)
    private Integer productId;

    @Column(name = "seller_id", columnDefinition = "INT UNSIGNED")
    private Integer sellerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private Seller seller;

    @Column(name = "category_id", columnDefinition = "BIGINT UNSIGNED")
    private Long categoryId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProductStatusEnum status;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Builder.Default
    @Column(name = "is_visible")
    private Boolean isVisible = true; 
    
}

