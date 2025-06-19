package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object cho dữ liệu đã trích xuất
 * Chứa dữ liệu từ nhiều nguồn cho quá trình ETL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedData {
    
    private LocalDate extractionDate;
    private List<OrderData> orders;
    private List<CustomerActivity> customerActivities;
    private List<ProductMetrics> productMetrics;
    private List<SellerMetrics> sellerMetrics;
    private DataQualityReport qualityReport;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderData {
        private Integer orderId;
        private Integer userId;
        private Integer sellerId;
        private BigDecimal totalAmount;
        private String status;
        private String productCategory;
        private LocalDate orderDate;
        private String paymentMethod;
        private String deliveryAddress;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerActivity {
        private Integer userId;
        private String activityType; // LOGIN, VIEW_PRODUCT, ADD_TO_CART, PURCHASE
        private String productCategory;
        private LocalDate activityDate;
        private Map<String, Object> activityData;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductMetrics {
        private Integer productId;
        private String productName;
        private String category;
        private Integer viewCount;
        private Integer purchaseCount;
        private BigDecimal revenue;
        private Double averageRating;
        private Integer stockLevel;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerMetrics {
        private Integer sellerId;
        private String sellerName;
        private Integer totalOrders;
        private BigDecimal totalRevenue;
        private Double averageRating;
        private Integer activeProducts;
        private String status;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataQualityReport {
        private Integer totalRecords;
        private Integer validRecords;
        private Integer invalidRecords;
        private Double qualityScore;
        private List<String> qualityIssues;
        private Map<String, Integer> fieldValidationResults;
    }
}
