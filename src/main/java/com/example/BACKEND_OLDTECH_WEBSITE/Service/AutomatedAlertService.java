package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminDashboard;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.AdminAlertService;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.AdminDashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Automated Alert Generation Service
 * Tự động tạo cảnh báo dựa trên metrics và KPI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutomatedAlertService {

    private final AdminAlertService adminAlertService;
    private final AdminDashboardRepository adminDashboardRepository;
    private final EntityManager entityManager;

    // ===============================
    // SCHEDULED MONITORING TASKS
    // ===============================

    /**
     * Chạy mỗi 5 phút để kiểm tra system health
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void monitorSystemHealth() {
        try {
            log.info("Starting system health monitoring...");
            
            // Monitor database connection
            checkDatabaseHealth();
            
            // Monitor system performance
            checkSystemPerformance();
            
            log.info("System health monitoring completed");
            
        } catch (Exception e) {
            log.error("Error in system health monitoring", e);
            
            // Create alert for monitoring failure
            adminAlertService.createSystemHealthAlert(
                "monitoring_system",
                "health_check_failure",
                BigDecimal.ONE,
                BigDecimal.ZERO
            );
        }
    }

    /**
     * Chạy mỗi 15 phút để kiểm tra business KPI
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    @Transactional
    public void monitorBusinessKPIs() {
        try {
            log.info("Starting business KPI monitoring...");
            
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            
            // Check conversion rate
            checkConversionRate(today);
            
            // Check order volume
            checkOrderVolume(today, yesterday);
            
            // Check GMV trends
            checkGMVTrends(today, yesterday);
            
            log.info("Business KPI monitoring completed");
            
        } catch (Exception e) {
            log.error("Error in business KPI monitoring", e);
        }
    }

    /**
     * Chạy mỗi giờ để kiểm tra fraud
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void monitorFraudActivities() {
        try {
            log.info("Starting fraud monitoring...");
            
            LocalDateTime startTime = LocalDateTime.now().minusHours(1);
            LocalDateTime endTime = LocalDateTime.now();
            
            // Check high-value transactions
            checkHighValueTransactions(startTime, endTime);
            
            // Check suspicious user patterns
            checkSuspiciousUserPatterns(startTime, endTime);
            
            log.info("Fraud monitoring completed");
            
        } catch (Exception e) {
            log.error("Error in fraud monitoring", e);
        }
    }

    /**
     * Chạy mỗi 30 phút để check pending items
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    @Transactional
    public void monitorPendingItems() {
        try {
            log.info("Starting pending items monitoring...");
            
            // Check pending sellers
            checkPendingSellers();
            
            // Check pending products
            checkPendingProducts();
            
            // Check unresolved complaints
            checkUnresolvedComplaints();
            
            log.info("Pending items monitoring completed");
            
        } catch (Exception e) {
            log.error("Error in pending items monitoring", e);
        }
    }

    // ===============================
    // SYSTEM HEALTH CHECKS
    // ===============================

    private void checkDatabaseHealth() {
        try {
            // Test database connectivity
            Query testQuery = entityManager.createNativeQuery("SELECT 1");
            testQuery.getSingleResult();
            
            // Check database performance
            long startTime = System.currentTimeMillis();
            Query perfQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM user LIMIT 1000");
            perfQuery.getSingleResult();
            long queryTime = System.currentTimeMillis() - startTime;
            
            if (queryTime > 5000) { // > 5 seconds
                adminAlertService.createPerformanceAlert(
                    "database_query_time",
                    new BigDecimal(queryTime),
                    new BigDecimal("5000")
                );
            }
            
        } catch (Exception e) {
            log.error("Database health check failed", e);
            adminAlertService.createSystemHealthAlert(
                "database",
                "connectivity_failure",
                BigDecimal.ZERO,
                BigDecimal.ONE
            );
        }
    }

    private void checkSystemPerformance() {
        try {
            // Check memory usage (mock - in real implementation use JVM metrics)
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            if (memoryUsagePercent > 85) {
                adminAlertService.createSystemHealthAlert(
                    "application_server",
                    "memory_usage_percent",
                    new BigDecimal(memoryUsagePercent),
                    new BigDecimal("85")
                );
            }
            
        } catch (Exception e) {
            log.error("System performance check failed", e);
        }
    }

    // ===============================
    // BUSINESS KPI CHECKS
    // ===============================

    private void checkConversionRate(LocalDate date) {
        try {            Optional<AdminDashboard> dashboardOpt = adminDashboardRepository.findByReportDateAndPeriodType(date, "DAILY");
            
            if (dashboardOpt.isPresent()) {
                AdminDashboard dashboard = dashboardOpt.get();
                BigDecimal conversionRate = dashboard.getConversionRate();
                
                if (conversionRate != null && conversionRate.compareTo(new BigDecimal("2.0")) < 0) {
                    adminAlertService.createBusinessKpiAlert(
                        "conversion_rate",
                        conversionRate,
                        new BigDecimal("3.0"),
                        "daily"
                    );
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking conversion rate", e);
        }
    }

    private void checkOrderVolume(LocalDate today, LocalDate yesterday) {
        try {
            Query todayQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM orders WHERE DATE(order_time) = ?1");
            todayQuery.setParameter(1, today);
            Number todayOrders = (Number) todayQuery.getSingleResult();
            
            Query yesterdayQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM orders WHERE DATE(order_time) = ?1");
            yesterdayQuery.setParameter(1, yesterday);
            Number yesterdayOrders = (Number) yesterdayQuery.getSingleResult();
            
            if (todayOrders != null && yesterdayOrders != null) {
                int todayCount = todayOrders.intValue();
                int yesterdayCount = yesterdayOrders.intValue();
                
                // Alert if orders drop by more than 30%
                if (yesterdayCount > 0) {
                    double dropPercent = ((double) (yesterdayCount - todayCount) / yesterdayCount) * 100;
                    if (dropPercent > 30) {
                        adminAlertService.createBusinessKpiAlert(
                            "daily_orders",
                            new BigDecimal(todayCount),
                            new BigDecimal(yesterdayCount),
                            "daily"
                        );
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking order volume", e);
        }
    }

    private void checkGMVTrends(LocalDate today, LocalDate yesterday) {
        try {
            Query todayQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE DATE(order_time) = ?1");
            todayQuery.setParameter(1, today);
            Number todayGMV = (Number) todayQuery.getSingleResult();
            
            Query yesterdayQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE DATE(order_time) = ?1");
            yesterdayQuery.setParameter(1, yesterday);
            Number yesterdayGMV = (Number) yesterdayQuery.getSingleResult();
            
            if (todayGMV != null && yesterdayGMV != null) {
                BigDecimal todayAmount = new BigDecimal(todayGMV.toString());
                BigDecimal yesterdayAmount = new BigDecimal(yesterdayGMV.toString());
                
                // Alert if GMV drops by more than 25%
                if (yesterdayAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal dropPercent = yesterdayAmount.subtract(todayAmount)
                            .divide(yesterdayAmount, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    
                    if (dropPercent.compareTo(new BigDecimal("25")) > 0) {
                        adminAlertService.createBusinessKpiAlert(
                            "daily_gmv",
                            todayAmount,
                            yesterdayAmount,
                            "daily"
                        );
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking GMV trends", e);
        }
    }

    // ===============================
    // FRAUD DETECTION CHECKS
    // ===============================

    private void checkHighValueTransactions(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Query highValueQuery = entityManager.createNativeQuery(
                "SELECT o.order_id, o.user_id, o.total_amount " +
                "FROM orders o " +
                "WHERE o.order_time BETWEEN ?1 AND ?2 " +
                "AND o.total_amount > 100000000"); // > 100M VND
            highValueQuery.setParameter(1, startTime);
            highValueQuery.setParameter(2, endTime);
            
            @SuppressWarnings("unchecked")
            List<Object[]> highValueOrders = highValueQuery.getResultList();
              for (Object[] order : highValueOrders) {
                Long orderId = ((Number) order[0]).longValue();
                // Long userId = ((Number) order[1]).longValue(); // Commented out as not used
                BigDecimal amount = new BigDecimal(order[2].toString());
                
                // Calculate risk score (simplified)
                BigDecimal riskScore = amount.divide(new BigDecimal("1000000"), 2, java.math.RoundingMode.HALF_UP);
                if (riskScore.compareTo(new BigDecimal("100")) > 0) {
                    riskScore = new BigDecimal("100");
                }
                
                if (riskScore.compareTo(new BigDecimal("80")) > 0) {
                    adminAlertService.createFraudAlert(
                        "HIGH_VALUE_TRANSACTION",
                        String.format("High-value transaction detected: Order ID %d, Amount: %s VND", orderId, amount),
                        "order",
                        orderId,
                        riskScore
                    );
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking high-value transactions", e);
        }
    }    private void checkSuspiciousUserPatterns(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // First check if login_attempt table exists
            Query checkTableQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = 'login_attempt'");
            Number tableExists = (Number) checkTableQuery.getSingleResult();
            
            if (tableExists.intValue() == 0) {
                log.debug("Login attempt table does not exist, skipping suspicious user pattern check");
                return;
            }
            
            // Check for multiple failed login attempts
            Query failedLoginQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM login_attempt " +
                "WHERE attempt_time BETWEEN ?1 AND ?2 " +
                "AND success = false " +
                "GROUP BY ip_address " +
                "HAVING COUNT(*) > 10");
            failedLoginQuery.setParameter(1, startTime);
            failedLoginQuery.setParameter(2, endTime);
            
            @SuppressWarnings("unchecked")
            List<Number> suspiciousIPs = failedLoginQuery.getResultList();
            
            if (!suspiciousIPs.isEmpty()) {
                adminAlertService.createSecurityAlert(
                    "BRUTE_FORCE_ATTACK",
                    String.format("Multiple failed login attempts detected from %d IP addresses", suspiciousIPs.size()),
                    "ip_address",
                    null
                );
            }
            
        } catch (Exception e) {
            log.warn("Error checking suspicious user patterns: {}", e.getMessage());
        }
    }

    // ===============================
    // PENDING ITEMS CHECKS
    // ===============================

    private void checkPendingSellers() {
        try {
            Query pendingQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM seller WHERE account_status = 'PENDING'");
            Number pendingCount = (Number) pendingQuery.getSingleResult();
            
            if (pendingCount != null && pendingCount.intValue() > 20) {
                adminAlertService.createBusinessKpiAlert(
                    "pending_sellers",
                    new BigDecimal(pendingCount.intValue()),
                    new BigDecimal("10"),
                    "current"
                );
            }
            
        } catch (Exception e) {
            log.error("Error checking pending sellers", e);
        }
    }

    private void checkPendingProducts() {
        try {
            Query pendingQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM product WHERE status = 'PENDING'");
            Number pendingCount = (Number) pendingQuery.getSingleResult();
            
            if (pendingCount != null && pendingCount.intValue() > 50) {
                adminAlertService.createBusinessKpiAlert(
                    "pending_products",
                    new BigDecimal(pendingCount.intValue()),
                    new BigDecimal("30"),
                    "current"
                );
            }
            
        } catch (Exception e) {
            log.error("Error checking pending products", e);
        }
    }

    private void checkUnresolvedComplaints() {
        try {
            Query unresolvedQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM complaint WHERE status = 'PENDING' AND created_at < ?1");
            unresolvedQuery.setParameter(1, LocalDateTime.now().minusDays(3)); // Older than 3 days
            Number unresolvedCount = (Number) unresolvedQuery.getSingleResult();
            
            if (unresolvedCount != null && unresolvedCount.intValue() > 10) {
                adminAlertService.createBusinessKpiAlert(
                    "unresolved_complaints",
                    new BigDecimal(unresolvedCount.intValue()),
                    new BigDecimal("5"),
                    "current"
                );
            }
            
        } catch (Exception e) {
            log.error("Error checking unresolved complaints", e);
        }
    }

    /**
     * Manual trigger cho testing
     */
    public void triggerAllMonitoring() {
        log.info("Manually triggering all monitoring tasks...");
        
        monitorSystemHealth();
        monitorBusinessKPIs();
        monitorFraudActivities();
        monitorPendingItems();
        
        log.info("All monitoring tasks completed");
    }
}
