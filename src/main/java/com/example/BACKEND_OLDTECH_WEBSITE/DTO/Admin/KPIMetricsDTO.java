package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KPIMetricsDTO {
    private LocalDate metricDate;
    private String metricType;
    
    // Financial KPIs
    private BigDecimal gmv;
    private BigDecimal aov;
    private BigDecimal platformRevenue;    private Integer totalOrders;
    
    // User Metrics
    private Integer totalVisits;
    private Integer newUsers;
    private BigDecimal conversionRate;
    private BigDecimal returningCustomersRate;
    
    // Seller/Product Metrics
    private Integer pendingSellers;
    private Integer approvedSellers;
    private Integer rejectedSellers;
    private Integer pendingProducts;
    private Integer activeProducts;
    
    // Geographic and Performance
    private String topProvince;
    private Integer suspiciousTransactions;
    private Integer newComplaints;
}
