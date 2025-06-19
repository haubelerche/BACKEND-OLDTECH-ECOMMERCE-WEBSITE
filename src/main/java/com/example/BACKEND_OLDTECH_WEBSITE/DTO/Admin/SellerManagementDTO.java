package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerManagementDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String status;
    private String approvalStatus;
    private BigDecimal commissionRate;
    private LocalDateTime lastLoginAt;
    private Integer loginCount;
    private LocalDateTime createdAt;
    private Integer totalProducts;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    
    // For seller approval
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerApprovalRequest {
        private Long sellerId;
        private String action; // "approve" or "reject"
        private String reason;
        private BigDecimal commissionRate;
    }
    
    // For seller action
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerActionRequest {
        private Long sellerId;
        private String action; // "lock", "unlock", "reset_password", "update_commission"
        private String reason;
        private BigDecimal newCommissionRate;
    }
}
