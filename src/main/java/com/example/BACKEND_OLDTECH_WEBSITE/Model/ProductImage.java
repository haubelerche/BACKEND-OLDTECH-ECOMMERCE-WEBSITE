package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "product_image")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private Integer imageId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product productId;

    @Column(name = "image_url", length = 2048, nullable = false)
    
    private String imageUrl;

    @Column(name = "is_thumbnail", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isThumbnail;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;
}