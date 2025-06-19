package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin.*;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminAlert;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminKPIMetrics;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.*;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service("legacyAdminDashboardService")
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("unchecked")
public class AdminDashboardService {private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;    private final ComplaintRepository complaintRepository;
    private final AdminKPIMetricsRepository adminKPIMetricsRepository;
    private final AdminAlertRepository adminAlertRepository;
    private final ComplaintAdapterService complaintAdapterService;

    // ===============================
    // I. BẢNG ĐIỀU KHIỂN TỔNG QUAN (Master Dashboard)
    // ===============================

    public KPIMetricsDTO getKPIMetrics(String period, LocalDate date) {
        Optional<AdminKPIMetrics> metricsOpt = adminKPIMetricsRepository
                .findByMetricDateAndMetricType(date, AdminKPIMetrics.MetricType.valueOf(period));
        
        if (metricsOpt.isEmpty()) {
            // Trigger refresh if no data found
            refreshKPIs(date);
            metricsOpt = adminKPIMetricsRepository
                    .findByMetricDateAndMetricType(date, AdminKPIMetrics.MetricType.valueOf(period));
        }
        
        AdminKPIMetrics metrics = metricsOpt.orElse(new AdminKPIMetrics());
          return new KPIMetricsDTO(
                metrics.getMetricDate(),
                metrics.getMetricType().name(),
                metrics.getGmv(),
                metrics.getAov(),
                metrics.getPlatformRevenue(),
                metrics.getTotalOrders(),
                metrics.getTotalVisits(),
                metrics.getNewUsers(),
                metrics.getConversionRate(),
                metrics.getReturningCustomersRate(),
                metrics.getPendingSellers(),
                metrics.getApprovedSellers(),
                metrics.getRejectedSellers(),
                metrics.getPendingProducts(),
                metrics.getActiveProducts(),
                metrics.getTopProvince(),
                metrics.getSuspiciousTransactions(),
                metrics.getNewComplaints()
        );
    }

    public void refreshKPIs(LocalDate date) {
        try {
            // Call stored procedure to refresh KPIs
            Query query = entityManager.createNativeQuery("CALL refresh_daily_kpis(?1)");
            query.setParameter(1, date);
            query.executeUpdate();
        } catch (Exception e) {
            // If stored procedure fails, calculate manually
            calculateKPIsManually(date);
        }
    }

    private void calculateKPIsManually(LocalDate date) {
        AdminKPIMetrics metrics = adminKPIMetricsRepository
                .findByMetricDateAndMetricType(date, AdminKPIMetrics.MetricType.daily)
                .orElse(new AdminKPIMetrics());

        metrics.setMetricDate(date);
        metrics.setMetricType(AdminKPIMetrics.MetricType.daily);

        // Calculate GMV and total orders
        Query gmvQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(total_amount), 0), COUNT(*) FROM orders " +
                "WHERE DATE(created_at) = ?1 AND status IN ('completed', 'delivered')");
        gmvQuery.setParameter(1, date);
        Object[] gmvResult = (Object[]) gmvQuery.getSingleResult();
        
        BigDecimal gmv = new BigDecimal(gmvResult[0].toString());
        Integer totalOrders = Integer.parseInt(gmvResult[1].toString());
        
        metrics.setGmv(gmv);
        metrics.setTotalOrders(totalOrders);
        metrics.setAov(totalOrders > 0 ? gmv.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);
        metrics.setPlatformRevenue(gmv.multiply(BigDecimal.valueOf(0.05))); // 5% commission

        // Calculate new users
        Query newUsersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM users WHERE DATE(created_at) = ?1");
        newUsersQuery.setParameter(1, date);
        Integer newUsers = Integer.parseInt(newUsersQuery.getSingleResult().toString());
        metrics.setNewUsers(newUsers);

        // Calculate pending sellers
        Query pendingSellersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM users WHERE role = 'SELLER' AND approval_status = 'pending'");
        Integer pendingSellers = Integer.parseInt(pendingSellersQuery.getSingleResult().toString());
        metrics.setPendingSellers(pendingSellers);

        // Calculate new complaints
        Query newComplaintsQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM complaints WHERE DATE(created_at) = ?1");
        newComplaintsQuery.setParameter(1, date);
        Integer newComplaints = Integer.parseInt(newComplaintsQuery.getSingleResult().toString());
        metrics.setNewComplaints(newComplaints);

        adminKPIMetricsRepository.save(metrics);
    }

    public ChartDataDTO getRevenueChart(String timeRange, int periods) {
        String sql = getChartSQL("SUM(total_amount)", "orders", timeRange, periods);
        
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        
        List<ChartDataDTO.ChartPointDTO> chartPoints = results.stream()
                .map(row -> new ChartDataDTO.ChartPointDTO(
                        row[0].toString(), // date label
                        Double.parseDouble(row[1].toString()), // value
                        null // timestamp will be set based on label
                ))
                .collect(Collectors.toList());
        
        return new ChartDataDTO("Revenue", "line", chartPoints, timeRange);
    }

    public ChartDataDTO getOrdersChart(String timeRange, int periods) {
        String sql = getChartSQL("COUNT(*)", "orders", timeRange, periods);
        
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        
        List<ChartDataDTO.ChartPointDTO> chartPoints = results.stream()
                .map(row -> new ChartDataDTO.ChartPointDTO(
                        row[0].toString(),
                        Double.parseDouble(row[1].toString()),
                        null
                ))
                .collect(Collectors.toList());
        
        return new ChartDataDTO("Orders", "bar", chartPoints, timeRange);
    }

    public ChartDataDTO getUsersChart(String timeRange, int periods) {
        String sql = getChartSQL("COUNT(*)", "users", timeRange, periods);
        
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        
        List<ChartDataDTO.ChartPointDTO> chartPoints = results.stream()
                .map(row -> new ChartDataDTO.ChartPointDTO(
                        row[0].toString(),
                        Double.parseDouble(row[1].toString()),
                        null
                ))
                .collect(Collectors.toList());
        
        return new ChartDataDTO("New Users", "line", chartPoints, timeRange);
    }

    private String getChartSQL(String selectClause, String tableName, String timeRange, int periods) {
        String dateFormat;
        String interval;
        
        switch (timeRange.toLowerCase()) {
            case "daily":
                dateFormat = "%Y-%m-%d";
                interval = "DAY";
                break;
            case "weekly":
                dateFormat = "%Y-%u";
                interval = "WEEK";
                break;
            case "monthly":
                dateFormat = "%Y-%m";
                interval = "MONTH";
                break;
            default:
                dateFormat = "%Y-%m";
                interval = "MONTH";
        }
        
        return String.format(
                "SELECT DATE_FORMAT(created_at, '%s') as period, %s as value " +
                "FROM %s " +
                "WHERE created_at >= DATE_SUB(NOW(), INTERVAL %d %s) " +
                "GROUP BY period " +
                "ORDER BY period",
                dateFormat, selectClause, tableName, periods, interval
        );
    }

    public List<Map<String, Object>> getGeographicOrderDensity(String period) {
        String sql = "SELECT " +
                "a.province, " +
                "COUNT(o.id) as order_count, " +
                "SUM(o.total_amount) as total_revenue " +
                "FROM orders o " +
                "JOIN addresses a ON o.shipping_address_id = a.id " +
                "WHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 1 " + period.toUpperCase() + ") " +
                "GROUP BY a.province " +
                "ORDER BY order_count DESC";
        
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("province", row[0]);
                    map.put("orderCount", row[1]);
                    map.put("totalRevenue", row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    public Page<AdminAlertDTO> getAdminAlerts(String alertType, String priority, boolean unreadOnly, Pageable pageable) {
        // This would be implemented with proper repository methods
        // For now, returning empty page
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public void markAlertAsRead(Long alertId) {
        Optional<AdminAlert> alertOpt = adminAlertRepository.findById(alertId);
        if (alertOpt.isPresent()) {
            AdminAlert alert = alertOpt.get();
            alert.setIsRead(true);
            adminAlertRepository.save(alert);
        }
    }

    public void resolveAlert(Long alertId, String resolution) {
        Optional<AdminAlert> alertOpt = adminAlertRepository.findById(alertId);
        if (alertOpt.isPresent()) {
            AdminAlert alert = alertOpt.get();
            alert.setIsResolved(true);
            alert.setResolvedAt(LocalDateTime.now());
            // alert.setResolution(resolution); // Add this field to model if needed
            adminAlertRepository.save(alert);
        }
    }

    public Map<String, Object> getFinancialReport(LocalDate startDate, LocalDate endDate, String groupBy) {
        Map<String, Object> report = new HashMap<>();
        
        String sql = "SELECT " +
                "DATE_FORMAT(created_at, '" + getDateFormat(groupBy) + "') as period, " +
                "SUM(total_amount) as revenue, " +
                "COUNT(*) as orders, " +
                "AVG(total_amount) as avg_order_value " +
                "FROM orders " +
                "WHERE DATE(created_at) BETWEEN ?1 AND ?2 " +
                "AND status IN ('completed', 'delivered') " +
                "GROUP BY period " +
                "ORDER BY period";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        
        List<Object[]> results = query.getResultList();
        report.put("timeSeriesData", results);
        
        // Calculate totals
        String totalSql = "SELECT SUM(total_amount), COUNT(*), AVG(total_amount) " +
                "FROM orders " +
                "WHERE DATE(created_at) BETWEEN ?1 AND ?2 " +
                "AND status IN ('completed', 'delivered')";
        
        Query totalQuery = entityManager.createNativeQuery(totalSql);
        totalQuery.setParameter(1, startDate);
        totalQuery.setParameter(2, endDate);
        
        Object[] totals = (Object[]) totalQuery.getSingleResult();
        report.put("totalRevenue", totals[0]);
        report.put("totalOrders", totals[1]);
        report.put("averageOrderValue", totals[2]);
        
        return report;
    }

    public Map<String, Object> getSalesReport(LocalDate startDate, LocalDate endDate, int topN) {
        Map<String, Object> report = new HashMap<>();
        
        // Top products
        String productSql = "SELECT p.name, SUM(oi.quantity) as total_sold, SUM(oi.price * oi.quantity) as revenue " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE DATE(o.created_at) BETWEEN ?1 AND ?2 " +
                "AND o.status IN ('completed', 'delivered') " +
                "GROUP BY p.id, p.name " +
                "ORDER BY total_sold DESC " +
                "LIMIT " + topN;
        
        Query productQuery = entityManager.createNativeQuery(productSql);
        productQuery.setParameter(1, startDate);
        productQuery.setParameter(2, endDate);
        
        report.put("topProducts", productQuery.getResultList());
        
        // Top sellers
        String sellerSql = "SELECT u.username, COUNT(DISTINCT o.id) as total_orders, SUM(o.total_amount) as revenue " +
                "FROM orders o " +
                "JOIN users u ON o.seller_id = u.id " +
                "WHERE DATE(o.created_at) BETWEEN ?1 AND ?2 " +
                "AND o.status IN ('completed', 'delivered') " +
                "GROUP BY u.id, u.username " +
                "ORDER BY revenue DESC " +
                "LIMIT " + topN;
        
        Query sellerQuery = entityManager.createNativeQuery(sellerSql);
        sellerQuery.setParameter(1, startDate);
        sellerQuery.setParameter(2, endDate);
        
        report.put("topSellers", sellerQuery.getResultList());
        
        return report;
    }

    public Map<String, Object> getUserAnalyticsReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // User registration trends
        String registrationSql = "SELECT DATE(created_at) as date, COUNT(*) as new_users " +
                "FROM users " +
                "WHERE DATE(created_at) BETWEEN ?1 AND ?2 " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY date";
        
        Query registrationQuery = entityManager.createNativeQuery(registrationSql);
        registrationQuery.setParameter(1, startDate);
        registrationQuery.setParameter(2, endDate);
        
        report.put("registrationTrend", registrationQuery.getResultList());
        
        // User activity analysis
        String activitySql = "SELECT " +
                "COUNT(DISTINCT CASE WHEN role = 'CUSTOMER' THEN id END) as total_customers, " +
                "COUNT(DISTINCT CASE WHEN role = 'SELLER' THEN id END) as total_sellers, " +
                "COUNT(DISTINCT CASE WHEN last_login_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN id END) as active_users " +
                "FROM users";
        
        Query activityQuery = entityManager.createNativeQuery(activitySql);
        Object[] activityResult = (Object[]) activityQuery.getSingleResult();
        
        report.put("totalCustomers", activityResult[0]);
        report.put("totalSellers", activityResult[1]);
        report.put("activeUsers", activityResult[2]);
        
        return report;
    }

    private String getDateFormat(String groupBy) {
        switch (groupBy.toLowerCase()) {
            case "daily":
                return "%Y-%m-%d";
            case "weekly":
                return "%Y-%u";
            case "monthly":
                return "%Y-%m";
            case "yearly":
                return "%Y";
            default:
                return "%Y-%m";
        }
    }

    // ===============================
    // II. QUẢN LÝ NGƯỜI BÁN (Seller Management)
    // ===============================

    public Page<SellerManagementDTO> getSellers(String status, String approvalStatus, String search, Pageable pageable) {
        // This would be implemented with proper repository methods and specifications
        // For now, returning empty page
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public SellerManagementDTO getSellerDetails(Long sellerId) {
        // Implementation would fetch seller details with related data
        return new SellerManagementDTO();
    }

    public void processSeller(SellerManagementDTO.SellerApprovalRequest request) {
        // Implementation for seller approval/rejection
    }

    public void performSellerAction(SellerManagementDTO.SellerActionRequest request) {
        // Implementation for seller actions (lock/unlock/reset password/update commission)
    }

    // ===============================
    // Placeholder methods for other features
    // ===============================

    public Page<Map<String, Object>> getCustomers(String status, String search, Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public Map<String, Object> getCustomerDetails(Long customerId) {
        return new HashMap<>();
    }

    public void toggleCustomerStatus(Long customerId, String action, String reason) {
        // Implementation
    }

    public Page<Map<String, Object>> getPendingProducts(Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public void approveProduct(Long productId, String action, String reason) {
        // Implementation
    }

    public Page<Map<String, Object>> getProducts(String search, String status, String category, Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public void removeProduct(Long productId, String reason) {
        // Implementation
    }

    public List<Map<String, Object>> getCategories() {
        return new ArrayList<>();
    }

    public void createCategory(Map<String, Object> categoryData) {
        // Implementation
    }

    public void updateCategory(Long categoryId, Map<String, Object> categoryData) {
        // Implementation
    }

    public void deleteCategory(Long categoryId) {
        // Implementation
    }

    public Page<Map<String, Object>> getOrders(String status, LocalDate startDate, LocalDate endDate, 
                                                String customerSearch, String sellerSearch, Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public void interveneOrder(Long orderId, String newStatus, String reason) {
        // Implementation
    }

    public Page<Map<String, Object>> getTransactions(LocalDate startDate, LocalDate endDate, String type, Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public Page<Map<String, Object>> getReturnRequests(String status, String type, Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    public void processReturnRequest(Long returnId, String action, String adminNotes) {
        // Implementation
    }    public Page<ComplaintManagementDTO> getComplaints(String status, String type, String priority, Pageable pageable) {
        return complaintAdapterService.getComplaints(status, type, priority, pageable);
    }

    public ComplaintManagementDTO getComplaintDetails(Long complaintId) {
        return complaintAdapterService.getComplaintDetails(complaintId);
    }

    public void processComplaint(ComplaintManagementDTO.ComplaintActionRequest request) {
        complaintAdapterService.processComplaint(request);
    }    public void assignComplaint(Long complaintId, Long adminId) {
        complaintAdapterService.assignComplaint(complaintId, adminId);
    }

    // ===============================
    // CÁC PHƯƠNG THỨC MỚI ĐƯỢC BỔ SUNG
    // ===============================

    /**
     * Lấy KPIs hiệu suất bán hàng chi tiết
     */
    public Map<String, Object> getSalesPerformanceKPIs(String period, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate fromDate = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate toDate = endDate != null ? endDate : LocalDate.now();
        
        try {
            // Calculate GMV (Gross Merchandise Value)
            Query gmvQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(o.total_amount), 0) FROM orders o " +
                "WHERE DATE(o.created_at) BETWEEN ?1 AND ?2 AND o.status IN ('completed', 'delivered')");
            gmvQuery.setParameter(1, fromDate);
            gmvQuery.setParameter(2, toDate);
            BigDecimal gmv = (BigDecimal) gmvQuery.getSingleResult();
            
            // Calculate AOV (Average Order Value)
            Query aovQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(AVG(o.total_amount), 0) FROM orders o " +
                "WHERE DATE(o.created_at) BETWEEN ?1 AND ?2 AND o.status IN ('completed', 'delivered')");
            aovQuery.setParameter(1, fromDate);
            aovQuery.setParameter(2, toDate);
            BigDecimal aov = (BigDecimal) aovQuery.getSingleResult();
            
            // Calculate Platform Revenue (commission from orders)
            Query revenueQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(o.total_amount * 0.05), 0) FROM orders o " +
                "WHERE DATE(o.created_at) BETWEEN ?1 AND ?2 AND o.status IN ('completed', 'delivered')");
            revenueQuery.setParameter(1, fromDate);
            revenueQuery.setParameter(2, toDate);
            BigDecimal platformRevenue = (BigDecimal) revenueQuery.getSingleResult();
            
            // Calculate Total Orders
            Query ordersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM orders o WHERE DATE(o.created_at) BETWEEN ?1 AND ?2");
            ordersQuery.setParameter(1, fromDate);
            ordersQuery.setParameter(2, toDate);            Long totalOrders = ((Number) ordersQuery.getSingleResult()).longValue();
            
            // Returns Count - Table removed, set to 0
            Long returnsCount = 0L;
            
            result.put("gmv", gmv);
            result.put("aov", aov);
            result.put("platformRevenue", platformRevenue);
            result.put("totalOrders", totalOrders);
            result.put("returnsCount", returnsCount);
            result.put("period", period);
            result.put("startDate", fromDate);
            result.put("endDate", toDate);
            
        } catch (Exception e) {
            // Return empty data on error
            result.put("gmv", BigDecimal.ZERO);
            result.put("aov", BigDecimal.ZERO);
            result.put("platformRevenue", BigDecimal.ZERO);
            result.put("totalOrders", 0L);
            result.put("returnsCount", 0L);
        }
        
        return result;
    }

    /**
     * Lấy KPIs hiệu suất người dùng chi tiết
     */
    public Map<String, Object> getUserPerformanceKPIs(String period, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate fromDate = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate toDate = endDate != null ? endDate : LocalDate.now();
        
        try {
            // Calculate Website Visits (mock data - would come from analytics service)
            Long totalVisits = 50000L + (long)(Math.random() * 10000);
            
            // Calculate New Users
            Query newUsersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM users u WHERE DATE(u.created_at) BETWEEN ?1 AND ?2");
            newUsersQuery.setParameter(1, fromDate);
            newUsersQuery.setParameter(2, toDate);
            Long newUsers = ((Number) newUsersQuery.getSingleResult()).longValue();
            
            // Calculate Conversion Rate (orders / visits * 100)
            Query ordersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM orders o WHERE DATE(o.created_at) BETWEEN ?1 AND ?2");
            ordersQuery.setParameter(1, fromDate);
            ordersQuery.setParameter(2, toDate);
            Long totalOrders = ((Number) ordersQuery.getSingleResult()).longValue();
              BigDecimal conversionRate = totalVisits > 0 ? 
                new BigDecimal(totalOrders * 100.0 / totalVisits).setScale(2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            // Calculate Returning Customers Rate
            Query returningQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT o1.user_id) FROM orders o1 " +
                "WHERE DATE(o1.created_at) BETWEEN ?1 AND ?2 " +
                "AND EXISTS (SELECT 1 FROM orders o2 WHERE o2.user_id = o1.user_id AND o2.created_at < o1.created_at)");
            returningQuery.setParameter(1, fromDate);
            returningQuery.setParameter(2, toDate);
            Long returningCustomers = ((Number) returningQuery.getSingleResult()).longValue();
              BigDecimal returningCustomersRate = totalOrders > 0 ? 
                new BigDecimal(returningCustomers * 100.0 / totalOrders).setScale(2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            result.put("totalVisits", totalVisits);
            result.put("newUsers", newUsers);
            result.put("conversionRate", conversionRate);
            result.put("returningCustomersRate", returningCustomersRate);
            result.put("period", period);
            result.put("startDate", fromDate);
            result.put("endDate", toDate);
            
        } catch (Exception e) {
            result.put("totalVisits", 0L);
            result.put("newUsers", 0L);
            result.put("conversionRate", BigDecimal.ZERO);
            result.put("returningCustomersRate", BigDecimal.ZERO);
        }
        
        return result;
    }

    /**
     * Lấy dữ liệu dự đoán ARIMA
     */
    public Map<String, Object> getARIMAPredictions(int forecastMonths, int historicalMonths) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Historical data for last N months
            List<Map<String, Object>> historicalUsers = getHistoricalData("users", historicalMonths);
            List<Map<String, Object>> historicalOrders = getHistoricalData("orders", historicalMonths);
            List<Map<String, Object>> historicalRevenue = getHistoricalData("revenue", historicalMonths);
            
            // Forecast data (simple mock implementation - in real world would use ARIMA algorithm)
            List<Map<String, Object>> forecastUsers = generateForecastData(historicalUsers, forecastMonths);
            List<Map<String, Object>> forecastOrders = generateForecastData(historicalOrders, forecastMonths);
            List<Map<String, Object>> forecastRevenue = generateForecastData(historicalRevenue, forecastMonths);
            
            Map<String, Object> usersData = new HashMap<>();
            usersData.put("historical", historicalUsers);
            usersData.put("forecast", forecastUsers);
            
            Map<String, Object> ordersData = new HashMap<>();
            ordersData.put("historical", historicalOrders);
            ordersData.put("forecast", forecastOrders);
            
            Map<String, Object> revenueData = new HashMap<>();
            revenueData.put("historical", historicalRevenue);
            revenueData.put("forecast", forecastRevenue);
            
            result.put("users", usersData);
            result.put("orders", ordersData);
            result.put("revenue", revenueData);
            result.put("forecastMonths", forecastMonths);
            result.put("historicalMonths", historicalMonths);
            
        } catch (Exception e) {
            result.put("error", "Failed to generate ARIMA predictions");
        }
        
        return result;
    }

    /**
     * Lấy xu hướng tỷ lệ chuyển đổi và đổi trả
     */
    public ChartDataDTO getConversionTrends(String timeRange, int periods) {
        List<ChartDataDTO.ChartPointDTO> dataPoints = new ArrayList<>();
        
        try {
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusMonths(i);
                
                // Mock conversion rate calculation
                Double conversionRate = 2.5 + (Math.random() * 2.0); // 2.5% to 4.5%
                
                dataPoints.add(new ChartDataDTO.ChartPointDTO(
                    date.toString(),
                    conversionRate,
                    date.atStartOfDay()
                ));
            }
        } catch (Exception e) {
            // Return empty chart on error
        }
        
        return new ChartDataDTO("Conversion Trends", "line", dataPoints, timeRange);
    }

    /**
     * Lấy xu hướng tỷ lệ khách hàng quay lại
     */
    public ChartDataDTO getCustomerRetentionTrends(String timeRange, int periods) {
        List<ChartDataDTO.ChartPointDTO> dataPoints = new ArrayList<>();
        
        try {
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusMonths(i);
                
                // Mock retention rate calculation
                Double retentionRate = 20.0 + (Math.random() * 15.0); // 20% to 35%
                
                dataPoints.add(new ChartDataDTO.ChartPointDTO(
                    date.toString(),
                    retentionRate,
                    date.atStartOfDay()
                ));
            }
        } catch (Exception e) {
            // Return empty chart on error
        }
        
        return new ChartDataDTO("Customer Retention Trends", "line", dataPoints, timeRange);
    }

    /**
     * Lấy heatmap mật độ đơn hàng theo địa lý
     */
    public List<Map<String, Object>> getGeographicHeatmap(String period, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        LocalDate fromDate = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate toDate = endDate != null ? endDate : LocalDate.now();
        
        try {
            Query query = entityManager.createNativeQuery(
                "SELECT a.province, COUNT(o.id) as order_count, SUM(o.total_amount) as total_value " +
                "FROM orders o " +
                "JOIN addresses a ON o.shipping_address_id = a.id " +
                "WHERE DATE(o.created_at) BETWEEN ?1 AND ?2 " +
                "GROUP BY a.province " +
                "ORDER BY order_count DESC");
            query.setParameter(1, fromDate);
            query.setParameter(2, toDate);
            
            List<Object[]> results = query.getResultList();
            
            for (Object[] row : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("province", row[0]);
                item.put("orderCount", row[1]);
                item.put("totalValue", row[2]);
                result.add(item);
            }
            
        } catch (Exception e) {
            // Return empty list on error
        }
        
        return result;
    }

    /**
     * Lấy biểu đồ lượt truy cập website
     */
    public ChartDataDTO getWebsiteVisitsChart(String timeRange, int periods) {
        List<ChartDataDTO.ChartPointDTO> dataPoints = new ArrayList<>();
        
        try {
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate date = "daily".equals(timeRange) ? 
                    LocalDate.now().minusDays(i) : 
                    LocalDate.now().minusMonths(i);
                
                // Mock website visits data
                Double visits = 1000.0 + (Math.random() * 2000.0);
                
                dataPoints.add(new ChartDataDTO.ChartPointDTO(
                    date.toString(),
                    visits,
                    date.atStartOfDay()
                ));
            }
        } catch (Exception e) {
            // Return empty chart on error
        }
        
        return new ChartDataDTO("Website Visits", "line", dataPoints, timeRange);
    }

    /**
     * Lấy biểu đồ đơn đổi trả hàng
     */
    public ChartDataDTO getReturnsChart(String timeRange, int periods) {
        List<ChartDataDTO.ChartPointDTO> dataPoints = new ArrayList<>();
        
        try {
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusMonths(i);
                LocalDate startOfMonth = date.withDayOfMonth(1);
                LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());                
                // Return count - Table removed, set to 0
                Long returnCount = 0L;
                
                dataPoints.add(new ChartDataDTO.ChartPointDTO(
                    date.toString(),
                    returnCount.doubleValue(),
                    date.atStartOfDay()
                ));
            }
        } catch (Exception e) {
            // Return empty chart on error
        }
        
        return new ChartDataDTO("Returns Chart", "line", dataPoints, timeRange);
    }

    /**
     * Lấy danh sách người bán chờ duyệt cho cảnh báo
     */
    public List<Map<String, Object>> getPendingSellersForAlert() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            Query query = entityManager.createNativeQuery(
                "SELECT u.id, u.username, u.email, u.created_at " +
                "FROM users u " +
                "WHERE u.role = 'SELLER' AND u.approval_status = 'PENDING' " +
                "ORDER BY u.created_at DESC LIMIT 10");
            
            List<Object[]> results = query.getResultList();
            
            for (Object[] row : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row[0]);
                item.put("username", row[1]);
                item.put("email", row[2]);
                item.put("createdAt", row[3]);
                result.add(item);
            }
            
        } catch (Exception e) {
            // Return empty list on error
        }
        
        return result;
    }

    /**
     * Lấy cảnh báo giao dịch gian lận
     */
    public List<Map<String, Object>> getFraudTransactionAlerts(int hoursBack) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            LocalDateTime fromTime = LocalDateTime.now().minusHours(hoursBack);
            
            Query query = entityManager.createNativeQuery(
                "SELECT o.id, o.total_amount, u.username, o.created_at " +
                "FROM orders o " +
                "JOIN users u ON o.user_id = u.id " +
                "WHERE o.created_at >= ?1 " +
                "AND (o.total_amount > 10000000 OR u.id IN (SELECT user_id FROM orders GROUP BY user_id HAVING COUNT(*) > 10)) " +
                "ORDER BY o.created_at DESC LIMIT 20");
            query.setParameter(1, fromTime);
            
            List<Object[]> results = query.getResultList();
            
            for (Object[] row : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("orderId", row[0]);
                item.put("amount", row[1]);
                item.put("username", row[2]);
                item.put("createdAt", row[3]);
                item.put("alertType", "SUSPICIOUS_TRANSACTION");
                result.add(item);
            }
            
        } catch (Exception e) {
            // Return empty list on error
        }
        
        return result;
    }

    /**
     * Lấy cảnh báo hệ thống
     */
    public List<Map<String, Object>> getSystemAlerts() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // Mock system alerts - in real implementation would check system health
            Map<String, Object> alert1 = new HashMap<>();
            alert1.put("id", 1L);
            alert1.put("type", "SYSTEM_PERFORMANCE");
            alert1.put("message", "High database connection usage detected");
            alert1.put("severity", "WARNING");
            alert1.put("createdAt", LocalDateTime.now().minusHours(2));
            result.add(alert1);
            
            Map<String, Object> alert2 = new HashMap<>();
            alert2.put("id", 2L);
            alert2.put("type", "DISK_SPACE");
            alert2.put("message", "Server disk space usage above 80%");
            alert2.put("severity", "INFO");
            alert2.put("createdAt", LocalDateTime.now().minusHours(5));
            result.add(alert2);
            
        } catch (Exception e) {
            // Return empty list on error
        }
        
        return result;
    }

    /**
     * Lấy báo cáo khiếu nại mới
     */
    public List<Map<String, Object>> getNewComplaintAlerts(int hoursBack) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            LocalDateTime fromTime = LocalDateTime.now().minusHours(hoursBack);
            
            Query query = entityManager.createNativeQuery(
                "SELECT c.id, c.title, c.type, c.priority, c.created_at, u.username " +
                "FROM complaints c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.created_at >= ?1 AND c.status = 'OPEN' " +
                "ORDER BY c.created_at DESC LIMIT 10");
            query.setParameter(1, fromTime);
            
            List<Object[]> results = query.getResultList();
            
            for (Object[] row : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row[0]);
                item.put("title", row[1]);
                item.put("type", row[2]);
                item.put("priority", row[3]);
                item.put("createdAt", row[4]);
                item.put("username", row[5]);
                result.add(item);
            }
            
        } catch (Exception e) {
            // Return empty list on error
        }
        
        return result;
    }

    /**
     * Tổng hợp tất cả cảnh báo
     */
    public Map<String, Object> getAlertsSummary() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("pendingSellersCount", getPendingSellersForAlert().size());
            result.put("newComplaintsCount", getNewComplaintAlerts(24).size());
            result.put("systemAlertsCount", getSystemAlerts().size());
            result.put("fraudTransactionsCount", getFraudTransactionAlerts(24).size());
            
            int totalCritical = (Integer) result.get("pendingSellersCount") +
                              (Integer) result.get("newComplaintsCount") +
                              (Integer) result.get("systemAlertsCount") +
                              (Integer) result.get("fraudTransactionsCount");
            
            result.put("totalCriticalAlerts", totalCritical);
            result.put("lastUpdated", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("error", "Failed to generate alerts summary");
        }
        
        return result;
    }

    /**
     * Lấy thống kê nhanh cho dashboard header
     */
    public Map<String, Object> getQuickStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Today's stats
            LocalDate today = LocalDate.now();
            
            Query todayOrdersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*), COALESCE(SUM(total_amount), 0) FROM orders WHERE DATE(created_at) = ?1");
            todayOrdersQuery.setParameter(1, today);
            Object[] todayOrders = (Object[]) todayOrdersQuery.getSingleResult();
            
            Query todayUsersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM users WHERE DATE(created_at) = ?1");
            todayUsersQuery.setParameter(1, today);
            Long todayUsers = ((Number) todayUsersQuery.getSingleResult()).longValue();
            
            result.put("todayOrders", todayOrders[0]);
            result.put("todayRevenue", todayOrders[1]);
            result.put("todayNewUsers", todayUsers);
            result.put("activeAlerts", getAlertsSummary().get("totalCriticalAlerts"));
            result.put("lastUpdated", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("error", "Failed to get quick stats");
        }
        
        return result;
    }

    /**
     * So sánh hiệu suất theo kỳ
     */
    public Map<String, Object> getPeriodComparison(String period) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate currentStart, currentEnd, previousStart, previousEnd;
            
            if ("month".equals(period)) {
                currentStart = LocalDate.now().withDayOfMonth(1);
                currentEnd = LocalDate.now();
                previousStart = currentStart.minusMonths(1);
                previousEnd = currentStart.minusDays(1);
            } else if ("quarter".equals(period)) {
                // Quarter comparison logic
                int currentQuarter = (LocalDate.now().getMonthValue() - 1) / 3;
                currentStart = LocalDate.now().withMonth(currentQuarter * 3 + 1).withDayOfMonth(1);
                currentEnd = LocalDate.now();
                previousStart = currentStart.minusMonths(3);
                previousEnd = currentStart.minusDays(1);
            } else { // year
                currentStart = LocalDate.now().withDayOfYear(1);
                currentEnd = LocalDate.now();
                previousStart = currentStart.minusYears(1);
                previousEnd = currentStart.minusDays(1);
            }
            
            // Get current period data
            Map<String, Object> currentData = getSalesPerformanceKPIs(period, currentStart, currentEnd);
            Map<String, Object> previousData = getSalesPerformanceKPIs(period, previousStart, previousEnd);
            
            result.put("current", currentData);
            result.put("previous", previousData);
            result.put("period", period);
            
            // Calculate growth rates
            BigDecimal currentRevenue = (BigDecimal) currentData.get("platformRevenue");
            BigDecimal previousRevenue = (BigDecimal) previousData.get("platformRevenue");
            
            if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {                BigDecimal growthRate = currentRevenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
                result.put("revenueGrowthRate", growthRate);
            } else {
                result.put("revenueGrowthRate", BigDecimal.ZERO);
            }
            
        } catch (Exception e) {
            result.put("error", "Failed to generate period comparison");
        }
        
        return result;
    }

    /**
     * Lấy top performers (categories và products)
     */
    public Map<String, Object> getTopPerformers(String period, int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate fromDate = LocalDate.now().minusDays("monthly".equals(period) ? 30 : 7);
            LocalDate toDate = LocalDate.now();
            
            // Top categories by revenue
            Query categoriesQuery = entityManager.createNativeQuery(
                "SELECT c.name, SUM(oi.price * oi.quantity) as revenue " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "JOIN categories c ON p.category_id = c.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE DATE(o.created_at) BETWEEN ?1 AND ?2 " +
                "GROUP BY c.id, c.name " +
                "ORDER BY revenue DESC LIMIT ?3");
            categoriesQuery.setParameter(1, fromDate);
            categoriesQuery.setParameter(2, toDate);
            categoriesQuery.setParameter(3, limit);
            
            List<Object[]> categoryResults = categoriesQuery.getResultList();
            List<Map<String, Object>> topCategories = new ArrayList<>();
            
            for (Object[] row : categoryResults) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", row[0]);
                item.put("revenue", row[1]);
                topCategories.add(item);
            }
            
            // Top products by sales
            Query productsQuery = entityManager.createNativeQuery(
                "SELECT p.name, SUM(oi.quantity) as total_sold, SUM(oi.price * oi.quantity) as revenue " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE DATE(o.created_at) BETWEEN ?1 AND ?2 " +
                "GROUP BY p.id, p.name " +
                "ORDER BY total_sold DESC LIMIT ?3");
            productsQuery.setParameter(1, fromDate);
            productsQuery.setParameter(2, toDate);
            productsQuery.setParameter(3, limit);
            
            List<Object[]> productResults = productsQuery.getResultList();
            List<Map<String, Object>> topProducts = new ArrayList<>();
            
            for (Object[] row : productResults) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", row[0]);
                item.put("totalSold", row[1]);
                item.put("revenue", row[2]);
                topProducts.add(item);
            }
            
            result.put("topCategories", topCategories);
            result.put("topProducts", topProducts);
            result.put("period", period);
            result.put("limit", limit);
            
        } catch (Exception e) {
            result.put("error", "Failed to get top performers");
        }
        
        return result;
    }

    /**
     * Export dữ liệu dashboard
     */
    public byte[] exportDashboardData(String format, LocalDate startDate, LocalDate endDate) {
        try {
            // Mock implementation - in real scenario would generate actual Excel/PDF
            String content = "Dashboard Export Data\n";
            content += "Format: " + format + "\n";
            content += "Date Range: " + startDate + " to " + endDate + "\n";
            content += "Generated at: " + LocalDateTime.now() + "\n";
            
            // Add KPI data
            Map<String, Object> salesKPIs = getSalesPerformanceKPIs("daily", startDate, endDate);
            content += "\nSales KPIs:\n";
            content += "GMV: " + salesKPIs.get("gmv") + "\n";
            content += "AOV: " + salesKPIs.get("aov") + "\n";
            content += "Platform Revenue: " + salesKPIs.get("platformRevenue") + "\n";
            
            return content.getBytes();
            
        } catch (Exception e) {
            return "Export failed".getBytes();
        }
    }

    // ===============================
    // HELPER METHODS
    // ===============================

    private List<Map<String, Object>> getHistoricalData(String type, int months) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            Map<String, Object> item = new HashMap<>();
            item.put("month", date.toString());
            
            // Mock data generation based on type
            switch (type) {
                case "users":
                    item.put("value", 1000 + (int)(Math.random() * 500));
                    break;
                case "orders":
                    item.put("value", 500 + (int)(Math.random() * 200));
                    break;
                case "revenue":
                    item.put("value", 50000 + (int)(Math.random() * 20000));
                    break;
            }
            
            result.add(item);
        }
        
        return result;
    }

    private List<Map<String, Object>> generateForecastData(List<Map<String, Object>> historical, int months) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (historical.isEmpty()) return result;
        
        // Simple trend calculation (last value + random growth)
        Map<String, Object> lastPoint = historical.get(historical.size() - 1);
        double lastValue = ((Number) lastPoint.get("value")).doubleValue();
        
        for (int i = 1; i <= months; i++) {
            LocalDate futureDate = LocalDate.now().plusMonths(i);
            Map<String, Object> item = new HashMap<>();
            item.put("month", futureDate.toString());
            
            // Simple forecast with slight upward trend and randomness
            double forecast = lastValue * (1.0 + 0.05 + (Math.random() * 0.1 - 0.05));
            item.put("value", Math.round(forecast));
            item.put("forecast", true);
            
            result.add(item);
            lastValue = forecast;
        }
        
        return result;
    }
}
