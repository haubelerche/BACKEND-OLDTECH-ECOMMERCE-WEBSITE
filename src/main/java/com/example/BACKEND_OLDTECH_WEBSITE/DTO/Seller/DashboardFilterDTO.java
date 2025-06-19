package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho request dashboard filter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFilterDTO {
    
    private String period; // "7d", "30d", "this_week", "last_week", "this_month", "last_month", "this_quarter", "last_quarter", "custom"
    private LocalDate startDate; // For custom range
    private LocalDate endDate; // For custom range
    private String chartType; // "line", "bar", "area"
    private String groupBy; // "day", "week", "month"
    private Boolean includeForecast; // Include forecast data or not
    
    // Validation methods
    public boolean isCustomPeriod() {
        return "custom".equals(period);
    }
    
    public boolean isValidCustomRange() {
        if (!isCustomPeriod()) return true;
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }
    
    public String getEffectiveGroupBy() {
        if (groupBy != null) return groupBy;
        
        // Auto-determine groupBy based on period
        switch (period) {
            case "7d":
            case "this_week":
            case "last_week":
                return "day";
            case "30d":
            case "this_month":
            case "last_month":
                return "day";
            case "this_quarter":
            case "last_quarter":
                return "week";
            default:
                return "day";
        }
    }
}
