package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;

import lombok.*;

import java.sql.Timestamp;


@Entity
@Table(name = "review")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", columnDefinition = "INT UNSIGNED", nullable = false)
    private Integer reviewId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "reviewer_id")
    private Integer reviewerId;

    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "review")
    private String review;

    @Column(name = "review_time")
    private Timestamp reviewTime;

    @Column(name = "seller_response")
    private String sellerResponse;

    @Column(name = "response_time")
    private Timestamp responseTime;
}

