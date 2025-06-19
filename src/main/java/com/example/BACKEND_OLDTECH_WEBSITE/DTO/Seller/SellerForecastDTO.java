package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho dự đoán ARIMA của seller dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerForecastDTO {
    
    private Integer sellerId;
    private LocalDate forecastDate;
    private String forecastType; // "ARIMA", "LINEAR", "EXPONENTIAL"
    
    // Dữ liệu lịch sử (9 tháng)
    private List<HistoricalData> historicalData;
    
    // Dữ liệu dự đoán (3 tháng)
    private List<ForecastData> forecastData;
    
    // Thông tin chất lượng dự đoán
    private ForecastQuality quality;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalData {
        private LocalDate date;
        private String monthLabel; // "Tháng 1/2024"
        private BigDecimal revenue;
        private Integer orders;
        private Integer visits;
        private BigDecimal conversionRate;
        private Boolean isActual; // true = dữ liệu thực, false = dự đoán
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastData {
        private LocalDate date;
        private String monthLabel; // "Tháng 4/2024"
        private BigDecimal predictedRevenue;
        private BigDecimal revenueConfidenceLower;
        private BigDecimal revenueConfidenceUpper;
        
        private Integer predictedOrders;
        private Integer ordersConfidenceLower;
        private Integer ordersConfidenceUpper;
        
        private Integer predictedVisits;
        private Integer visitsConfidenceLower;
        private Integer visitsConfidenceUpper;
        
        private BigDecimal confidenceLevel; // 0.95 for 95%
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastQuality {
        private BigDecimal accuracy; // 0-1 scale
        private BigDecimal mape; // Mean Absolute Percentage Error
        private BigDecimal rmse; // Root Mean Square Error
        private String qualityRating; // "EXCELLENT", "GOOD", "FAIR", "POOR"
        private List<String> qualityNotes;
    }
}
