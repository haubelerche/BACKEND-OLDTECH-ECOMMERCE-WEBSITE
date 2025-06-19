package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho admin dashboard overview
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardOverviewDTO {
    
    // Thông tin thời gian
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodLabel;
    
    // KPIs chính
    private PlatformMetrics currentPeriod;
    private PlatformMetrics previousPeriod;
    private GrowthMetrics growth;
    private AlertSummary alerts;
    
    // Biểu đồ xu hướng
    private List<TrendData> gmvTrend;
    private List<TrendData> orderTrend;
    private List<TrendData> userTrend;
    private List<TrendData> visitTrend;
    private List<TrendData> conversionTrend;
    private List<TrendData> returnTrend;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformMetrics {
        // Hiệu suất bán hàng
        private BigDecimal grossMerchandiseValue;
        private BigDecimal averageOrderValue;
        private BigDecimal platformRevenue;
        private Integer totalOrders;
        private Integer totalReturnOrders;
        private BigDecimal returnOrdersValue;
        private BigDecimal netGmv;
        
        // Hiệu suất người dùng
        private Integer websiteVisits;
        private Integer uniqueVisitors;
        private Integer newUsers;
        private Integer newBuyers;
        private Integer newSellers;
        private BigDecimal conversionRate;
        private Integer returningCustomerOrders;
        private BigDecimal customerReturnRate;
        private BigDecimal orderReturnRate;
        
        // Thông tin bổ sung
        private Integer activeSellers;
        private Integer activeBuyers;
        private Integer totalProducts;
        private Integer activeProducts;
        private BigDecimal commissionRate;
        private BigDecimal avgProcessingTime;
        private BigDecimal avgDeliveryTime;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthMetrics {
        private BigDecimal gmvGrowth;
        private BigDecimal aovGrowth;
        private BigDecimal revenueGrowth;
        private BigDecimal orderGrowth;
        private BigDecimal visitGrowth;
        private BigDecimal userGrowth;
        private BigDecimal conversionGrowth;
        private BigDecimal returnRateChange;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertSummary {
        private Integer pendingSellers;
        private Integer newComplaints;
        private Integer fraudTransactions;
        private Integer systemAlerts;
        private Integer pendingProducts;
        private Integer reportedProducts;
        private Integer totalAlerts;
        private String priority; // HIGH, MEDIUM, LOW
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private LocalDate date;
        private String label; // "2024-01-15", "Tuần 3", "Tháng 1"
        private BigDecimal value;
        private String displayValue; // Formatted value for display
        private String unit; // "VND", "đơn", "%", "lượt"
        private Boolean isForecast; // Dữ liệu dự đoán hay thực tế
    }
}
