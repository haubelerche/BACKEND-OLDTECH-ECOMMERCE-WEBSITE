package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho dự đoán ARIMA của admin dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminForecastDTO {
    
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
        private BigDecimal gmv;
        private Integer orders;
        private Integer users;
        private Integer visits;
        private BigDecimal platformRevenue;
        private BigDecimal conversionRate;
        private Boolean isActual; // true = dữ liệu thực, false = dự đoán
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastData {
        private LocalDate date;
        private String monthLabel; // "Tháng 4/2024"
        
        // GMV Forecast
        private BigDecimal predictedGmv;
        private BigDecimal gmvConfidenceLower;
        private BigDecimal gmvConfidenceUpper;
        
        // Orders Forecast
        private Integer predictedOrders;
        private Integer ordersConfidenceLower;
        private Integer ordersConfidenceUpper;
        
        // Users Forecast
        private Integer predictedUsers;
        private Integer usersConfidenceLower;
        private Integer usersConfidenceUpper;
        
        // Platform Revenue Forecast
        private BigDecimal predictedRevenue;
        private BigDecimal revenueConfidenceLower;
        private BigDecimal revenueConfidenceUpper;
        
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
        private String modelUsed; // "ARIMA", "LINEAR_REGRESSION", "EXPONENTIAL_SMOOTHING"
    }
}
