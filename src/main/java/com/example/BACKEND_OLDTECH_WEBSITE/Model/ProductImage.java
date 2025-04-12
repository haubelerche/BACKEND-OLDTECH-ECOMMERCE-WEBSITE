package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "product_image")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Integer imageId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product productId;

    @Column(name = "image_url", length = 2048)
    @NotBlank(message = "Thông tin không được để trống")
    private String imageUrl;

    @Column(name = "is_thumbnail", columnDefinition = "TINYINT(1)")
    private Boolean isThumbnail;

    @Column(name = "created_at")
    private Timestamp createdAt;
}