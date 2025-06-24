package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;

import java.sql.Timestamp;

/**
 * DTO for seller filtering response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerFilterResponse {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private AccountStatusEnum accountStatus;
    private RoleEnum role;
    private Boolean isVerified;
    private String refundMomoAccount;
    private String livingLocation;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Seller specific fields
    private Integer sellerId;
    private Integer businessStatus;
    private String businessStatusLabel;
    private Boolean isApproved;
    private String momoAccount;
    private Timestamp sellerCreatedAt;
    private Timestamp sellerUpdatedAt;
    
    // Statistics
    private Long totalProducts;
    private Long totalOrders;
    private java.math.BigDecimal totalRevenue;
    private Double averageRating;
    private Long totalReviews;
}
