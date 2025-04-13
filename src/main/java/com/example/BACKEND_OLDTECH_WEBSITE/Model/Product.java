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
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller sellerId;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String description;

    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatusEnum status;

    @Column(name = "is_approved", columnDefinition = "TINYINT(1)")
    private Boolean isApproved;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "listed_at", nullable = false)
    private Timestamp listedAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

@Column(name = "is_visible", columnDefinition = "TINYINT(1)")
    private boolean isVisible = true;

}