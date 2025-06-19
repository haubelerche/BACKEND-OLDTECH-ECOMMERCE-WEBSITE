package com.example.BACKEND_OLDTECH_WEBSITE.ETL.util;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Seller;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for creating test data for ETL tests
 * Simplified version focused on API testing
 */
public class ETLTestDataUtil {

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
    }    public static List<Orders> createTestOrders(int count) {
        List<Orders> orders = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            Orders order = new Orders();
            order.setOrderId(i);
            order.setUserId(i % 10 + 1); // Cycle through 10 users
            order.setTotalAmount(BigDecimal.valueOf(100.0 + (i * 25.0)));
            order.setStatus(i % 4 == 0 ? OrderStatusEnum.Cancelled : OrderStatusEnum.Delivered);
            order.setPaymentMethod(i % 2 == 0 ? PaymentMethodEnum.CashOnDelivery : PaymentMethodEnum.Momo);
            order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(i % 7)));
            order.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(i % 7)));
            
            orders.add(order);
        }
        
        return orders;
    }    public static List<Product> createTestProducts(int count) {
        List<Product> products = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            Product product = new Product();
            product.setProductId(i);
            product.setName("Test Product " + i);
            product.setDescription("Test description for product " + i);
            product.setPrice(BigDecimal.valueOf(500.0 + (i * 100.0)));
            product.setSellerId(i % 5 + 1);
            product.setCreatedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(i % 30)));
            
            products.add(product);
        }
        
        return products;
    }    public static List<User> createTestUsers(int count) {
        List<User> users = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            User user = new User();
            user.setUserId(i);
            user.setEmail("testuser" + i + "@gmail.com");
            user.setFirstName("Test");
            user.setLastName("User " + i);
            user.setPhoneNumber("+123456789" + String.format("%02d", i % 100));
            user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(i % 90)));
            
            users.add(user);
        }
        
        return users;
    }

    public static List<Seller> createTestSellers(int count) {
        List<Seller> sellers = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            Seller seller = new Seller();
            seller.setSellerId(i);
            seller.setShopName("Test Shop " + i);
            seller.setUserId(i); // Assuming sellers are also users
            seller.setIsApproved(i % 4 != 0); // 75% approved
            seller.setCreatedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(i % 60)));
            
            sellers.add(seller);
        }
        
        return sellers;
    }    /**
     * Create test data with relationships for comprehensive testing
     */
    public static class TestDataSet {
        private List<User> users;
        private List<Seller> sellers;
        private List<Product> products;
        private List<Orders> orders;

        public TestDataSet(int userCount, int sellerCount, int productCount, int orderCount) {
            this.users = createTestUsers(userCount);
            this.sellers = createTestSellers(sellerCount);
            this.products = createTestProducts(productCount);
            this.orders = createTestOrders(orderCount);
        }

        // Getters
        public List<User> getUsers() { return users; }
        public List<Seller> getSellers() { return sellers; }
        public List<Product> getProducts() { return products; }
        public List<Orders> getOrders() { return orders; }
    }

    /**
     * Create a standard test dataset
     */
    public static TestDataSet createStandardTestDataSet() {
        return new TestDataSet(20, 5, 15, 50);
    }

    /**
     * Create a large test dataset for performance testing
     */
    public static TestDataSet createLargeTestDataSet() {
        return new TestDataSet(1000, 100, 500, 2000);
    }

    /**
     * Create a minimal test dataset
     */
    public static TestDataSet createMinimalTestDataSet() {
        return new TestDataSet(5, 2, 3, 10);
    }
}
