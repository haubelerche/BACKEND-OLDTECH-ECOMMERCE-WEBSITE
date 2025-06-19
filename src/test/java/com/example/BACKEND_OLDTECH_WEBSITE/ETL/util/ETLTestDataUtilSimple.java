package com.example.BACKEND_OLDTECH_WEBSITE.ETL.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating test data for ETL tests
 * Simplified version focused on API testing
 */
public class ETLTestDataUtilSimple {

    /**
     * Get test date in string format for API calls
     */
    public static String getTestDateString() {
        return LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Get test date range for backfill testing
     */
    public static Map<String, String> getTestDateRange(int daysBack) {
        Map<String, String> dateRange = new HashMap<>();
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(daysBack);
        
        dateRange.put("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        dateRange.put("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        return dateRange;
    }

    /**
     * Get current date string for API calls
     */
    public static String getCurrentDateString() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Get future date string (should fail validation)
     */
    public static String getFutureDateString() {
        return LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Get invalid date string for testing error handling
     */
    public static String getInvalidDateString() {
        return "invalid-date-format";
    }

    /**
     * Sample seller IDs for testing
     */
    public static Integer[] getTestSellerIds() {
        return new Integer[]{1, 2, 3, 4, 5};
    }

    /**
     * Sample test parameters for ETL API calls
     */
    public static Map<String, Object> getETLTestParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("testDate", getTestDateString());
        params.put("currentDate", getCurrentDateString());
        params.put("futureDate", getFutureDateString());
        params.put("invalidDate", getInvalidDateString());
        params.put("sellerIds", getTestSellerIds());
        params.put("dateRange", getTestDateRange(7));
        
        return params;
    }

    /**
     * Mock ETL result data for testing
     */
    public static Map<String, Object> createMockETLResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("status", "SUCCESS");
        result.put("processDate", getTestDateString());
        result.put("duration", 300L);
        result.put("recordsExtracted", 100);
        result.put("recordsTransformed", 100);
        result.put("recordsLoaded", 100);
        result.put("dataQualityScore", 95.0);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }

    /**
     * Mock ETL failure result for testing
     */
    public static Map<String, Object> createMockETLFailure() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("status", "FAILED");
        result.put("processDate", getTestDateString());
        result.put("errorMessage", "Test error for unit testing");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }

    /**
     * Mock pipeline status for testing
     */
    public static Map<String, Object> createMockPipelineStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "HEALTHY");
        status.put("isHealthy", true);
        status.put("lastRun", "2024-01-15T10:00:00");
        status.put("nextScheduledRun", "2024-01-16T01:00:00");
        status.put("totalRuns", 100);
        status.put("successfulRuns", 95);
        status.put("failedRuns", 5);
        
        return status;
    }
}
