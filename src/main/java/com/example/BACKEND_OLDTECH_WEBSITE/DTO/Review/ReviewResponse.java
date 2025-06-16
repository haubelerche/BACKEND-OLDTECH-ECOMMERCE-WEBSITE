package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Review;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ReviewResponse {
    private Integer reviewId;
    private Integer orderId;
    private Integer reviewerId;
    private Integer sellerId;
    private Integer productId;
    private Integer rating;
    private String review;
    private Timestamp reviewTime;
    private String sellerResponse;
    private Timestamp responseTime;
}

