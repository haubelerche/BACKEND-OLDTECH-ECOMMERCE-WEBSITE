package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho dashboard overview của seller
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerDashboardOverviewDTO {
    
    // Thông tin thời gian
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodLabel;
    
    // KPIs chính
    private PerformanceMetrics currentPeriod;
    private PerformanceMetrics previousPeriod;
    private GrowthMetrics growth;
    
    // Biểu đồ xu hướng
    private List<TrendData> revenueTrend;
    private List<TrendData> orderTrend;
    private List<TrendData> visitTrend;
    private List<TrendData> conversionTrend;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        // Hiệu suất bán hàng
        private BigDecimal totalRevenue;
        private Integer totalOrders;
        private BigDecimal averageOrderValue;
        private Integer returnOrdersCount;
        private BigDecimal returnOrdersValue;
        private BigDecimal netRevenue;
        
        // Hiệu suất người dùng
        private Integer totalVisits;
        private Integer uniqueVisitors;
        private BigDecimal conversionRate;
        private Integer returningCustomerOrders;
        private Integer newCustomerOrders;
        private BigDecimal customerReturnRate;
        private BigDecimal returnRate;
        
        // Thông tin đơn hàng
        private Integer successfulOrders;
        private Integer cancelledOrders;
        private Integer pendingOrders;
        private Integer shippedOrders;
        
        // Sản phẩm
        private Integer productsSold;
        private Integer uniqueProductsSold;
        private BigDecimal avgDeliveryTime;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthMetrics {
        private BigDecimal revenueGrowth;
        private BigDecimal orderGrowth;
        private BigDecimal aovGrowth;
        private BigDecimal visitGrowth;
        private BigDecimal conversionGrowth;
        private BigDecimal customerReturnGrowth;
        private BigDecimal returnRateChange;
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
    }
}
