package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminDashboard;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.AdminDashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Service để ETL dữ liệu admin dashboard định kỳ
 * Chạy daily để tính toán và lưu trữ platform metrics
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminDashboardETLService {

    private final EntityManager entityManager;
    private final AdminDashboardRepository adminDashboardRepository;

    /**
     * Chạy ETL hàng ngày vào 1:00 AM (trước seller dashboard)
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void runDailyETL() {
        log.info("Starting daily admin dashboard ETL process");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            processAdminDashboardForDate(yesterday, "DAILY");
            
            // Cũng update dữ liệu weekly/monthly nếu cần
            if (yesterday.getDayOfWeek().getValue() == 7) { // Sunday
                processWeeklyData(yesterday);
            }
            
            if (yesterday.getDayOfMonth() == 1) { // First day of month
                processMonthlyData(yesterday.minusDays(1)); // Previous month
            }
            
            log.info("Daily admin dashboard ETL completed successfully");
        } catch (Exception e) {
            log.error("Error in daily admin dashboard ETL", e);
        }
    }

    /**
     * Process admin dashboard data cho một ngày cụ thể
     */
    public void processAdminDashboardForDate(LocalDate date, String periodType) {
        log.info("Processing admin dashboard data for {} ({})", date, periodType);
        
        try {
            // Check if data already exists
            adminDashboardRepository.findByReportDateAndPeriodType(date, periodType)
                .ifPresent(existing -> {
                    log.info("Data already exists for {} ({}), deleting before recreating", date, periodType);
                    adminDashboardRepository.delete(existing);
                });

            AdminDashboard dashboard = new AdminDashboard();
            dashboard.setReportDate(date);
            dashboard.setPeriodType(periodType);

            // Calculate all metrics
            calculateFinancialMetrics(dashboard, date, periodType);
            calculateOrderMetrics(dashboard, date, periodType);
            calculateUserMetrics(dashboard, date, periodType);
            calculateProductMetrics(dashboard, date, periodType);
            calculatePerformanceMetrics(dashboard, date, periodType);
            calculateAlertMetrics(dashboard, date, periodType);

            // Save to database
            adminDashboardRepository.save(dashboard);
            
            log.info("Admin dashboard data processed and saved for {} ({})", date, periodType);
            
        } catch (Exception e) {
            log.error("Error processing admin dashboard data for {} ({})", date, periodType, e);
            throw new RuntimeException("Failed to process admin dashboard data: " + e.getMessage());
        }
    }

    /**
     * Calculate financial metrics
     */
    private void calculateFinancialMetrics(AdminDashboard dashboard, LocalDate date, String periodType) {
        try {
            String dateCondition = getDateCondition(date, periodType);
              // Total Revenue (platform commission)
            Query revenueQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(o.total_amount * 0.05), 0) " + // 5% commission
                "FROM orders o " +
                "WHERE o.status = 'COMPLETED' AND " + dateCondition);
            setDateParameter(revenueQuery, date, periodType);
            BigDecimal totalRevenue = new BigDecimal(revenueQuery.getSingleResult().toString());
            dashboard.setPlatformRevenue(totalRevenue);

            // Gross Merchandise Value (GMV)
            Query gmvQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(o.total_amount), 0) " +
                "FROM orders o " +
                "WHERE o.status IN ('COMPLETED', 'DELIVERED') AND " + dateCondition);
            setDateParameter(gmvQuery, date, periodType);
            BigDecimal gmv = new BigDecimal(gmvQuery.getSingleResult().toString());
            dashboard.setGrossMerchandiseValue(gmv);

            // Average Order Value
            Query aovQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(AVG(o.total_amount), 0) " +
                "FROM orders o " +
                "WHERE o.status IN ('COMPLETED', 'DELIVERED') AND " + dateCondition);
            setDateParameter(aovQuery, date, periodType);
            BigDecimal averageOrderValue = new BigDecimal(aovQuery.getSingleResult().toString());
            dashboard.setAverageOrderValue(averageOrderValue);

            // Skip total refunds as not available in model

        } catch (Exception e) {
            log.error("Error calculating financial metrics", e);
        }
    }

    /**
     * Calculate order metrics
     */
    private void calculateOrderMetrics(AdminDashboard dashboard, LocalDate date, String periodType) {
        try {
            String dateCondition = getDateCondition(date, periodType);
              // Total Orders
            Query totalOrdersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) " +
                "FROM orders o " +
                "WHERE " + dateCondition);
            setDateParameter(totalOrdersQuery, date, periodType);
            Integer totalOrders = ((Number) totalOrdersQuery.getSingleResult()).intValue();
            dashboard.setTotalOrders(totalOrders);

            // Skip completed orders as not available in model

            // Skip cancelled orders as not available in model            // Skip order return rate calculation as completed orders not tracked

        } catch (Exception e) {
            log.error("Error calculating order metrics", e);
        }
    }

    /**
     * Calculate user metrics
     */
    private void calculateUserMetrics(AdminDashboard dashboard, LocalDate date, String periodType) {
        try {
            String dateCondition = getDateCondition(date, periodType);
            
            // New Users
            Query newUsersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM user u WHERE " + dateCondition.replace("order_time", "created_at"));
            setDateParameter(newUsersQuery, date, periodType);
            Integer newUsers = ((Number) newUsersQuery.getSingleResult()).intValue();
            dashboard.setNewUsers(newUsers);

            // Active Buyers (users who made orders)
            Query activeBuyersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT o.user_id) " +
                "FROM orders o " +
                "WHERE " + dateCondition);
            setDateParameter(activeBuyersQuery, date, periodType);
            Integer activeBuyers = ((Number) activeBuyersQuery.getSingleResult()).intValue();
            dashboard.setActiveBuyers(activeBuyers);

            // New Sellers
            Query newSellersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM seller s WHERE " + dateCondition.replace("order_time", "created_at"));
            setDateParameter(newSellersQuery, date, periodType);
            Integer newSellers = ((Number) newSellersQuery.getSingleResult()).intValue();
            dashboard.setNewSellers(newSellers);

            // Active Sellers (sellers who had orders)
            Query activeSellersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT p.seller_id) " +
                "FROM orders o " +
                "JOIN product p ON o.product_id = p.product_id " +
                "WHERE " + dateCondition);
            setDateParameter(activeSellersQuery, date, periodType);
            Integer activeSellers = ((Number) activeSellersQuery.getSingleResult()).intValue();
            dashboard.setActiveSellers(activeSellers);

            // Website Visits
            Query visitsQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(daily_visits), 0) FROM daily_statistics " +
                "WHERE " + dateCondition.replace("order_time", "stat_date"));
            setDateParameter(visitsQuery, date, periodType);
            Integer websiteVisits = ((Number) visitsQuery.getSingleResult()).intValue();
            dashboard.setWebsiteVisits(websiteVisits);
            
            // Conversion Rate
            if (websiteVisits > 0) {
                BigDecimal conversionRate = new BigDecimal(dashboard.getTotalOrders())
                    .divide(new BigDecimal(websiteVisits), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                dashboard.setConversionRate(conversionRate);
            }

            // Customer Return Rate
            Query returningCustomersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT o.user_id) " +
                "FROM orders o " +
                "WHERE o.user_id IN (" +
                "  SELECT DISTINCT o2.user_id FROM orders o2 " +
                "  WHERE o2.order_time < (SELECT MIN(o3.order_time) FROM orders o3 WHERE o3.user_id = o.user_id AND " + dateCondition.replace("o.", "o3.") + ")" +
                ") AND " + dateCondition);
            setDateParameter(returningCustomersQuery, date, periodType);
            Integer returningCustomers = ((Number) returningCustomersQuery.getSingleResult()).intValue();
            dashboard.setReturningCustomerOrders(returningCustomers);

            if (activeBuyers > 0) {
                BigDecimal customerReturnRate = new BigDecimal(returningCustomers)
                    .divide(new BigDecimal(activeBuyers), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                dashboard.setCustomerReturnRate(customerReturnRate);
            }

        } catch (Exception e) {
            log.error("Error calculating user metrics", e);
        }
    }

    /**
     * Calculate product metrics
     */
    private void calculateProductMetrics(AdminDashboard dashboard, LocalDate date, String periodType) {
        try {
            String dateCondition = getDateCondition(date, periodType);
            
            // Total Products
            Query totalProductsQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM product p WHERE " + dateCondition.replace("order_time", "created_at"));
            setDateParameter(totalProductsQuery, date, periodType);
            Integer totalProducts = ((Number) totalProductsQuery.getSingleResult()).intValue();
            dashboard.setTotalProducts(totalProducts);

            // Pending Products
            Query pendingProductsQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM product p WHERE p.status = 'PENDING' AND " + dateCondition.replace("order_time", "created_at"));
            setDateParameter(pendingProductsQuery, date, periodType);
            Integer pendingProducts = ((Number) pendingProductsQuery.getSingleResult()).intValue();
            dashboard.setPendingProducts(pendingProducts);

            // Reported Products
            Query reportedProductsQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT c.product_id) FROM complaint c WHERE c.complaint_type = 'PRODUCT_ISSUE' AND " + 
                dateCondition.replace("order_time", "created_at"));
            setDateParameter(reportedProductsQuery, date, periodType);
            Integer reportedProducts = ((Number) reportedProductsQuery.getSingleResult()).intValue();
            dashboard.setReportedProducts(reportedProducts);

        } catch (Exception e) {
            log.error("Error calculating product metrics", e);
        }
    }    /**
     * Calculate performance metrics (using available fields)
     */
    private void calculatePerformanceMetrics(AdminDashboard dashboard, LocalDate date, String periodType) {
        try {
            // Get unique visitors from daily statistics
            String dateCondition = getDateCondition(date, periodType);
            Query uniqueVisitorsQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(unique_visitors), 0) FROM daily_statistics " +
                "WHERE " + dateCondition.replace("order_time", "stat_date"));
            setDateParameter(uniqueVisitorsQuery, date, periodType);
            Integer uniqueVisitors = ((Number) uniqueVisitorsQuery.getSingleResult()).intValue();
            dashboard.setUniqueVisitors(uniqueVisitors);

            // Calculate average processing time (mock)
            BigDecimal avgProcessingTime = new BigDecimal("24.5"); // Mock value in hours
            dashboard.setAvgProcessingTime(avgProcessingTime);
            
            // Calculate average delivery time (mock)
            BigDecimal avgDeliveryTime = new BigDecimal("3.2"); // Mock value in days  
            dashboard.setAvgDeliveryTime(avgDeliveryTime);

        } catch (Exception e) {
            log.error("Error calculating performance metrics", e);
        }
    }

    /**
     * Calculate alert-related metrics
     */
    private void calculateAlertMetrics(AdminDashboard dashboard, LocalDate date, String periodType) {
        try {
            String dateCondition = getDateCondition(date, periodType);
            
            // Pending Sellers
            Query pendingSellersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM seller s WHERE s.account_status = 'PENDING'");
            Integer pendingSellers = ((Number) pendingSellersQuery.getSingleResult()).intValue();
            dashboard.setPendingSellers(pendingSellers);

            // New Complaints
            Query newComplaintsQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM complaint c WHERE " + dateCondition.replace("order_time", "created_at"));
            setDateParameter(newComplaintsQuery, date, periodType);
            Integer newComplaints = ((Number) newComplaintsQuery.getSingleResult()).intValue();
            dashboard.setNewComplaints(newComplaints);

            // Fraud Transactions (high-value orders)
            Query fraudQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM orders o " +
                "WHERE o.total_amount > 50000000 AND " + dateCondition); // > 50M VND
            setDateParameter(fraudQuery, date, periodType);
            Integer fraudTransactions = ((Number) fraudQuery.getSingleResult()).intValue();
            dashboard.setFraudTransactions(fraudTransactions);

            // System Alerts (placeholder)
            dashboard.setSystemAlerts(0);

        } catch (Exception e) {
            log.error("Error calculating alert metrics", e);
        }
    }

    /**
     * Get date condition based on period type
     */
    private String getDateCondition(LocalDate date, String periodType) {
        switch (periodType.toUpperCase()) {
            case "DAILY":
                return "DATE(order_time) = ?1";
            case "WEEKLY":
                return "YEARWEEK(order_time, 1) = YEARWEEK(?1, 1)";
            case "MONTHLY":
                return "YEAR(order_time) = YEAR(?1) AND MONTH(order_time) = MONTH(?1)";
            default:
                return "DATE(order_time) = ?1";
        }
    }

    /**
     * Set date parameter for query
     */
    private void setDateParameter(Query query, LocalDate date, String periodType) {
        query.setParameter(1, date);
    }

    /**
     * Process weekly data
     */
    private void processWeeklyData(LocalDate endDate) {
        try {
            log.info("Processing weekly admin dashboard data for week ending {}", endDate);
            processAdminDashboardForDate(endDate, "WEEKLY");
        } catch (Exception e) {
            log.error("Error processing weekly admin dashboard data", e);
        }
    }

    /**
     * Process monthly data
     */
    private void processMonthlyData(LocalDate endDate) {
        try {
            log.info("Processing monthly admin dashboard data for month ending {}", endDate);
            processAdminDashboardForDate(endDate, "MONTHLY");
        } catch (Exception e) {
            log.error("Error processing monthly admin dashboard data", e);
        }
    }

    /**
     * Manual trigger để chạy ETL cho một ngày cụ thể
     */
    public void runETLForDate(LocalDate date) {
        log.info("Running manual admin dashboard ETL for date {}", date);
        
        try {
            processAdminDashboardForDate(date, "DAILY");
            log.info("Manual admin dashboard ETL completed for date {}", date);
        } catch (Exception e) {
            log.error("Error in manual admin dashboard ETL for date {}", date, e);
            throw new RuntimeException("Admin dashboard ETL failed: " + e.getMessage());
        }
    }

    /**
     * Backfill ETL cho nhiều ngày
     */
    public void runBackfillETL(LocalDate startDate, LocalDate endDate) {
        log.info("Running admin dashboard backfill ETL from {} to {}", startDate, endDate);
        
        try {
            LocalDate currentDate = startDate;
            int processedDays = 0;
            
            while (!currentDate.isAfter(endDate)) {
                try {
                    processAdminDashboardForDate(currentDate, "DAILY");
                    processedDays++;
                    log.debug("Processed admin dashboard data for {}", currentDate);
                } catch (Exception e) {
                    log.error("Failed to process admin dashboard data for {}", currentDate, e);
                }
                
                currentDate = currentDate.plusDays(1);
            }
            
            log.info("Admin dashboard backfill ETL completed. Processed {} days", processedDays);
        } catch (Exception e) {
            log.error("Error in admin dashboard backfill ETL", e);
            throw new RuntimeException("Admin dashboard backfill ETL failed: " + e.getMessage());
        }
    }
}
