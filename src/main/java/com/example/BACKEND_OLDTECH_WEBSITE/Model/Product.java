package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

import java.math.BigDecimal;
import java.sql.Timestamp;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;

@Entity
@Table(name = "product")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller sellerId;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "name", length = 200)
    @NotBlank(message = "Thông tin không được để trống")
    private String name;

    @Column(name = "description", columnDefinition = "MEDIUMTEXT")
    @NotBlank(message = "Thông tin không được để trống")
    private String description;

    @Column(name = "price", precision = 12, scale = 2)
    @NotBlank(message = "Thông tin không được để trống")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductStatusEnum status;

    @Column(name = "is_approved", columnDefinition = "TINYINT(1)")
    private Boolean isApproved;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "listed_at")
    private Timestamp listedAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;


}