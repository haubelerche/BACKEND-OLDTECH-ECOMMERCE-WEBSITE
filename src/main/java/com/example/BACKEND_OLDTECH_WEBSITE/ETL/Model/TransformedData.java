package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object cho dữ liệu đã chuyển đổi và được bổ sung
 * Sẵn sàng để tải vào data warehouse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransformedData {
    
    private LocalDate processDate;
    private SalesMetrics salesMetrics;
    private CustomerSegments customerSegments;
    private ProductAnalytics productAnalytics;
    private BusinessKPIs businessKPIs;
    private List<AlertDefinition> dataQualityAlerts;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesMetrics {
        private BigDecimal totalRevenue;
        private Integer totalOrders;
        private Integer uniqueCustomers;
        private BigDecimal averageOrderValue;
        private Map<String, BigDecimal> revenueByCategory;
        private Map<String, Integer> ordersByStatus;
        private BigDecimal growthRate;
        private Integer cancelledOrders;
        private BigDecimal refundAmount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSegments {
        private List<Segment> segments;
        private Map<String, Double> retentionRates;
        private Map<String, BigDecimal> lifetimeValues;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Segment {
            private String segmentName; // NEW, RETURNING, VIP, INACTIVE
            private Integer customerCount;
            private BigDecimal avgOrderValue;
            private Double conversionRate;
            private List<String> characteristics;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAnalytics {
        private List<ProductPerformance> topPerformingProducts;
        private List<ProductPerformance> underPerformingProducts;
        private Map<String, TrendAnalysis> categoryTrends;
        private Map<String, Integer> stockAlerts;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ProductPerformance {
            private Integer productId;
            private String productName;
            private String category;
            private BigDecimal revenue;
            private Integer unitsSold;
            private Double conversionRate;
            private Double profitMargin;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TrendAnalysis {
            private String category;
            private String trendDirection; // UP, DOWN, STABLE
            private Double changePercentage;
            private List<String> factors;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessKPIs {
        private Double customerAcquisitionCost;
        private Double customerLifetimeValue;
        private Double churnRate;
        private Double conversionRate;
        private Integer newCustomers;
        private Integer returningCustomers;
        private BigDecimal monthlyRecurringRevenue;
        private Double orderFulfillmentRate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertDefinition {
        private String alertType; // WARNING, ERROR, INFO
        private String message;
        private String severity; // HIGH, MEDIUM, LOW
        private Map<String, Object> context;
        private LocalDate triggeredDate;
    }
}
