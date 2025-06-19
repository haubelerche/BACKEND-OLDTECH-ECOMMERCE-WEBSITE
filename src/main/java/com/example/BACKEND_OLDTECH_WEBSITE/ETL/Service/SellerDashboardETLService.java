package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.SellerDashboard;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.SellerDashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service để ETL dữ liệu dashboard seller định kỳ
 * Chạy daily để tính toán và lưu trữ metrics
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SellerDashboardETLService {

    private final EntityManager entityManager;
    private final SellerDashboardRepository sellerDashboardRepository;

    /**
     * Chạy ETL hàng ngày vào 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void runDailyETL() {
        log.info("Starting daily seller dashboard ETL process");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            processAllSellersForDate(yesterday, "DAILY");
            
            // Cũng update dữ liệu weekly/monthly nếu cần
            if (yesterday.getDayOfWeek().getValue() == 7) { // Sunday
                processWeeklyData(yesterday);
            }
            
            if (yesterday.getDayOfMonth() == 1) { // First day of month
                processMonthlyData(yesterday.minusDays(1)); // Previous month
            }
            
            log.info("Daily seller dashboard ETL completed successfully");
            
        } catch (Exception e) {
            log.error("Error in daily seller dashboard ETL", e);
        }
    }

    /**
     * Process tất cả seller cho một ngày cụ thể
     */
    public void processAllSellersForDate(LocalDate date, String periodType) {
        try {
            List<Integer> sellerIds = getAllActiveSellerIds();
            
            for (Integer sellerId : sellerIds) {
                try {
                    processSellerDashboardData(sellerId, date, periodType);
                } catch (Exception e) {
                    log.error("Error processing seller {} for date {}", sellerId, date, e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing all sellers for date {}", date, e);
        }
    }

    /**
     * Process dashboard data cho một seller cụ thể
     */
    public void processSellerDashboardData(Integer sellerId, LocalDate date, String periodType) {
        try {
            // Kiểm tra xem đã có data cho ngày này chưa
            boolean exists = sellerDashboardRepository.existsBySellerIdAndReportDateAndPeriodType(
                    sellerId, date, periodType);
            
            if (exists && !"DAILY".equals(periodType)) {
                log.debug("Dashboard data already exists for seller {} on date {} ({})", 
                         sellerId, date, periodType);
                return;
            }

            SellerDashboard dashboard = exists ? 
                    sellerDashboardRepository.findBySellerIdAndReportDateAndPeriodType(sellerId, date, periodType)
                            .orElse(new SellerDashboard()) :
                    new SellerDashboard();

            // Set basic info
            dashboard.setSellerId(sellerId);
            dashboard.setReportDate(date);
            dashboard.setPeriodType(periodType);
            dashboard.setIsForecast(false);

            // Calculate date range for period
            LocalDate startDate, endDate;
            switch (periodType) {
                case "DAILY":
                    startDate = endDate = date;
                    break;
                case "WEEKLY":
                    startDate = date.minusDays(6);
                    endDate = date;
                    break;
                case "MONTHLY":
                    startDate = date.withDayOfMonth(1);
                    endDate = date.withDayOfMonth(date.lengthOfMonth());
                    break;
                default:
                    startDate = endDate = date;
            }

            // Calculate sales metrics
            calculateSalesMetrics(dashboard, sellerId, startDate, endDate);
            
            // Calculate user metrics
            calculateUserMetrics(dashboard, sellerId, startDate, endDate);
            
            // Calculate derived metrics
            dashboard.calculateAverageOrderValue();
            dashboard.calculateConversionRate();
            dashboard.calculateCustomerReturnRate();
            dashboard.calculateReturnRate();
            dashboard.calculateNetRevenue();

            // Save dashboard data
            sellerDashboardRepository.save(dashboard);
            
            log.debug("Processed dashboard data for seller {} on date {} ({})", 
                     sellerId, date, periodType);

        } catch (Exception e) {
            log.error("Error processing seller dashboard data for seller {} on date {}", 
                     sellerId, date, e);
        }
    }

    /**
     * Tính toán sales metrics
     */
    private void calculateSalesMetrics(SellerDashboard dashboard, Integer sellerId, 
                                     LocalDate startDate, LocalDate endDate) {
        try {
            // Total Revenue from successful orders
            Query revenueQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(o.total_amount), 0) FROM orders o " +
                "JOIN order_detail od ON o.order_id = od.order_id " +
                "JOIN product p ON od.product_id = p.product_id " +
                "WHERE p.seller_id = ?1 " +
                "AND DATE(o.order_time) BETWEEN ?2 AND ?3 " +
                "AND o.status IN ('DELIVERED', 'COMPLETED')");
            revenueQuery.setParameter(1, sellerId);
            revenueQuery.setParameter(2, startDate);
            revenueQuery.setParameter(3, endDate);
            BigDecimal totalRevenue = (BigDecimal) revenueQuery.getSingleResult();
            dashboard.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

            // Total Orders
            Query ordersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT o.order_id) FROM orders o " +
                "JOIN order_detail od ON o.order_id = od.order_id " +
                "JOIN product p ON od.product_id = p.product_id " +
                "WHERE p.seller_id = ?1 " +
                "AND DATE(o.order_time) BETWEEN ?2 AND ?3");
            ordersQuery.setParameter(1, sellerId);
            ordersQuery.setParameter(2, startDate);
            ordersQuery.setParameter(3, endDate);
            Number totalOrdersNum = (Number) ordersQuery.getSingleResult();
            dashboard.setTotalOrders(totalOrdersNum != null ? totalOrdersNum.intValue() : 0);

            // Return Orders
            Query returnQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*), COALESCE(SUM(r.refund_amount), 0) FROM refund r " +
                "JOIN orders o ON r.order_id = o.order_id " +
                "JOIN order_detail od ON o.order_id = od.order_id " +
                "JOIN product p ON od.product_id = p.product_id " +
                "WHERE p.seller_id = ?1 " +
                "AND DATE(r.created_at) BETWEEN ?2 AND ?3 " +
                "AND r.status = 'APPROVED'");
            returnQuery.setParameter(1, sellerId);
            returnQuery.setParameter(2, startDate);
            returnQuery.setParameter(3, endDate);
            Object[] returnResult = (Object[]) returnQuery.getSingleResult();
            Number returnCount = (Number) returnResult[0];
            BigDecimal returnValue = (BigDecimal) returnResult[1];
            dashboard.setReturnOrdersCount(returnCount != null ? returnCount.intValue() : 0);
            dashboard.setReturnOrdersValue(returnValue != null ? returnValue : BigDecimal.ZERO);

            // Order Status Breakdown
            calculateOrderStatusBreakdown(dashboard, sellerId, startDate, endDate);

            // Products Sold
            calculateProductMetrics(dashboard, sellerId, startDate, endDate);

        } catch (Exception e) {
            log.error("Error calculating sales metrics", e);
        }
    }

    /**
     * Tính toán user metrics
     */
    private void calculateUserMetrics(SellerDashboard dashboard, Integer sellerId, 
                                    LocalDate startDate, LocalDate endDate) {
        try {
            // Estimate visits (simplified - in real app, integrate with analytics)
            Integer totalOrders = dashboard.getTotalOrders();
            Integer estimatedVisits = totalOrders != null ? (int) (totalOrders / 0.03) : 0; // 3% conversion
            dashboard.setTotalVisits(estimatedVisits);
            dashboard.setUniqueVisitors((int) (estimatedVisits * 0.8)); // 80% unique

            // Customer Segmentation
            calculateCustomerSegmentation(dashboard, sellerId, startDate, endDate);

        } catch (Exception e) {
            log.error("Error calculating user metrics", e);
        }
    }

    /**
     * Tính customer segmentation
     */
    private void calculateCustomerSegmentation(SellerDashboard dashboard, Integer sellerId, 
                                             LocalDate startDate, LocalDate endDate) {
        try {
            // Returning customers (customers who bought before)
            Query returningQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT current_orders.user_id) FROM orders current_orders " +
                "JOIN order_detail cod ON current_orders.order_id = cod.order_id " +
                "JOIN product cp ON cod.product_id = cp.product_id " +
                "WHERE cp.seller_id = ?1 " +
                "AND DATE(current_orders.order_time) BETWEEN ?2 AND ?3 " +
                "AND EXISTS (" +
                "  SELECT 1 FROM orders previous_orders " +
                "  JOIN order_detail pod ON previous_orders.order_id = pod.order_id " +
                "  JOIN product pp ON pod.product_id = pp.product_id " +
                "  WHERE pp.seller_id = ?1 " +
                "  AND previous_orders.user_id = current_orders.user_id " +
                "  AND DATE(previous_orders.order_time) < ?2" +
                ")");
            returningQuery.setParameter(1, sellerId);
            returningQuery.setParameter(2, startDate);
            returningQuery.setParameter(3, endDate);
            Number returningCustomers = (Number) returningQuery.getSingleResult();
            dashboard.setReturningCustomerOrders(returningCustomers != null ? returningCustomers.intValue() : 0);

            // New customers
            Integer totalOrders = dashboard.getTotalOrders();
            Integer newCustomers = totalOrders - dashboard.getReturningCustomerOrders();
            dashboard.setNewCustomerOrders(Math.max(0, newCustomers));

        } catch (Exception e) {
            log.error("Error calculating customer segmentation", e);
        }
    }

    /**
     * Tính order status breakdown
     */
    private void calculateOrderStatusBreakdown(SellerDashboard dashboard, Integer sellerId, 
                                             LocalDate startDate, LocalDate endDate) {
        try {
            Query statusQuery = entityManager.createNativeQuery(
                "SELECT o.status, COUNT(*) FROM orders o " +
                "JOIN order_detail od ON o.order_id = od.order_id " +
                "JOIN product p ON od.product_id = p.product_id " +
                "WHERE p.seller_id = ?1 " +
                "AND DATE(o.order_time) BETWEEN ?2 AND ?3 " +
                "GROUP BY o.status");
            statusQuery.setParameter(1, sellerId);
            statusQuery.setParameter(2, startDate);
            statusQuery.setParameter(3, endDate);

            @SuppressWarnings("unchecked")
            List<Object[]> statusResults = statusQuery.getResultList();
            
            int successful = 0, cancelled = 0, pending = 0, shipped = 0;
            
            for (Object[] result : statusResults) {
                String status = (String) result[0];
                Number count = (Number) result[1];
                int countValue = count != null ? count.intValue() : 0;
                
                switch (status.toUpperCase()) {
                    case "DELIVERED":
                    case "COMPLETED":
                        successful += countValue;
                        break;
                    case "CANCELLED":
                    case "REJECTED":
                        cancelled += countValue;
                        break;
                    case "PENDING":
                    case "PROCESSING":
                        pending += countValue;
                        break;
                    case "SHIPPED":
                    case "SHIPPING":
                        shipped += countValue;
                        break;
                }
            }
            
            dashboard.setSuccessfulOrders(successful);
            dashboard.setCancelledOrders(cancelled);
            dashboard.setPendingOrders(pending);
            dashboard.setShippedOrders(shipped);

        } catch (Exception e) {
            log.error("Error calculating order status breakdown", e);
        }
    }

    /**
     * Tính product metrics
     */
    private void calculateProductMetrics(SellerDashboard dashboard, Integer sellerId, 
                                       LocalDate startDate, LocalDate endDate) {
        try {
            // Total products sold
            Query productsSoldQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(od.quantity), 0) FROM order_detail od " +
                "JOIN product p ON od.product_id = p.product_id " +
                "JOIN orders o ON od.order_id = o.order_id " +
                "WHERE p.seller_id = ?1 " +
                "AND DATE(o.order_time) BETWEEN ?2 AND ?3 " +
                "AND o.status IN ('DELIVERED', 'COMPLETED')");
            productsSoldQuery.setParameter(1, sellerId);
            productsSoldQuery.setParameter(2, startDate);
            productsSoldQuery.setParameter(3, endDate);
            Number productsSold = (Number) productsSoldQuery.getSingleResult();
            dashboard.setProductsSold(productsSold != null ? productsSold.intValue() : 0);

            // Unique products sold
            Query uniqueProductsQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT od.product_id) FROM order_detail od " +
                "JOIN product p ON od.product_id = p.product_id " +
                "JOIN orders o ON od.order_id = o.order_id " +
                "WHERE p.seller_id = ?1 " +
                "AND DATE(o.order_time) BETWEEN ?2 AND ?3 " +
                "AND o.status IN ('DELIVERED', 'COMPLETED')");
            uniqueProductsQuery.setParameter(1, sellerId);
            uniqueProductsQuery.setParameter(2, startDate);
            uniqueProductsQuery.setParameter(3, endDate);
            Number uniqueProducts = (Number) uniqueProductsQuery.getSingleResult();
            dashboard.setUniqueProductsSold(uniqueProducts != null ? uniqueProducts.intValue() : 0);

            // Average delivery time (mock data)
            dashboard.setAvgDeliveryTime(BigDecimal.valueOf(4.2));

        } catch (Exception e) {
            log.error("Error calculating product metrics", e);
        }
    }

    /**
     * Process weekly data
     */
    private void processWeeklyData(LocalDate endDate) {
        try {
            log.info("Processing weekly dashboard data for week ending {}", endDate);
            processAllSellersForDate(endDate, "WEEKLY");
        } catch (Exception e) {
            log.error("Error processing weekly data", e);
        }
    }

    /**
     * Process monthly data
     */
    private void processMonthlyData(LocalDate endDate) {
        try {
            log.info("Processing monthly dashboard data for month ending {}", endDate);
            processAllSellersForDate(endDate, "MONTHLY");
        } catch (Exception e) {
            log.error("Error processing monthly data", e);
        }
    }

    /**
     * Lấy tất cả seller ID đang hoạt động
     */
    private List<Integer> getAllActiveSellerIds() {
        Query query = entityManager.createNativeQuery(
            "SELECT s.seller_id FROM seller s " +
            "JOIN user u ON s.user_id = u.user_id " +
            "WHERE s.account_status = 'ACTIVE' " +
            "AND u.is_active = true");
        
        @SuppressWarnings("unchecked")
        List<Number> results = query.getResultList();
        return results.stream()
                .map(Number::intValue)
                .toList();
    }

    /**
     * Manual trigger để chạy ETL cho một seller cụ thể
     */
    public void runETLForSeller(Integer sellerId, LocalDate date) {
        log.info("Running manual ETL for seller {} on date {}", sellerId, date);
        
        try {
            processSellerDashboardData(sellerId, date, "DAILY");
            log.info("Manual ETL completed for seller {} on date {}", sellerId, date);
        } catch (Exception e) {
            log.error("Error in manual ETL for seller {} on date {}", sellerId, date, e);
            throw new RuntimeException("ETL failed: " + e.getMessage());
        }
    }

    /**
     * Manual trigger để chạy ETL cho tất cả seller
     */
    public void runETLForAllSellers(LocalDate date) {
        log.info("Running manual ETL for all sellers on date {}", date);
        
        try {
            processAllSellersForDate(date, "DAILY");
            log.info("Manual ETL completed for all sellers on date {}", date);
        } catch (Exception e) {
            log.error("Error in manual ETL for all sellers on date {}", date, e);
            throw new RuntimeException("ETL failed: " + e.getMessage());
        }
    }
}
