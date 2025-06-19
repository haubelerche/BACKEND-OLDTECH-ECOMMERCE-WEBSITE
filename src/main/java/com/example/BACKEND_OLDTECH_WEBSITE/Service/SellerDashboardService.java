package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin.ChartDataDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("unchecked")
public class SellerDashboardService {

    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // ===============================
    // I. TRANG TỔNG QUAN (Overview Dashboard)
    // ===============================

    /**
     * Lấy KPIs tổng quan cho seller
     */
    public Map<String, Object> getSellerKPIs(String username, String period, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return createEmptyKPIs();
            }
            
            LocalDate[] dateRange = calculateDateRange(period, startDate, endDate);
            LocalDate fromDate = dateRange[0];
            LocalDate toDate = dateRange[1];
            
            // Get sales KPIs
            Map<String, Object> salesKPIs = getSalesPerformanceKPIs(username, period, fromDate, toDate);
            
            // Get user KPIs
            Map<String, Object> userKPIs = getUserPerformanceKPIs(username, period, fromDate, toDate);
            
            // Combine all KPIs
            result.put("sales", salesKPIs);
            result.put("users", userKPIs);
            result.put("period", period);
            result.put("dateRange", Map.of("from", fromDate, "to", toDate));
            result.put("lastUpdated", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("error", "Failed to fetch seller KPIs: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Lấy KPIs hiệu suất bán hàng chi tiết cho seller
     */
    public Map<String, Object> getSalesPerformanceKPIs(String username, String period, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return createEmptyKPIs();
            }
            
            LocalDate[] dateRange = calculateDateRange(period, startDate, endDate);
            LocalDate fromDate = dateRange[0];
            LocalDate toDate = dateRange[1];
            
            // Calculate Revenue
            Query revenueQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(oi.price * oi.quantity), 0) FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3 " +
                "AND o.status IN ('completed', 'delivered')");
            revenueQuery.setParameter(1, sellerId);
            revenueQuery.setParameter(2, fromDate);
            revenueQuery.setParameter(3, toDate);
            BigDecimal revenue = (BigDecimal) revenueQuery.getSingleResult();
            
            // Calculate Total Orders
            Query ordersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT o.id) FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3");
            ordersQuery.setParameter(1, sellerId);
            ordersQuery.setParameter(2, fromDate);
            ordersQuery.setParameter(3, toDate);
            Long totalOrders = ((Number) ordersQuery.getSingleResult()).longValue();
            
            // Calculate AOV (Average Order Value)
            BigDecimal aov = totalOrders > 0 ?                revenue.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            // Returns Count - Table removed, set to 0
            Long returnsCount = 0L;
            
            // Calculate previous period for comparison
            LocalDate[] prevDateRange = calculatePreviousPeriodRange(period, fromDate, toDate);
            Map<String, Object> previousKPIs = getPreviousPeriodKPIs(sellerId, prevDateRange[0], prevDateRange[1]);
            
            result.put("revenue", revenue);
            result.put("totalOrders", totalOrders);
            result.put("aov", aov);
            result.put("returnsCount", returnsCount);
            result.put("period", period);
            result.put("dateRange", Map.of("from", fromDate, "to", toDate));
            result.put("previousPeriod", previousKPIs);
            result.put("growthRates", calculateGrowthRates(result, previousKPIs));
            
        } catch (Exception e) {
            result = createEmptyKPIs();
            result.put("error", "Failed to fetch sales performance KPIs");
        }
        
        return result;
    }

    /**
     * Lấy KPIs hiệu suất người dùng chi tiết cho seller
     */
    public Map<String, Object> getUserPerformanceKPIs(String username, String period, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return createEmptyKPIs();
            }
            
            LocalDate[] dateRange = calculateDateRange(period, startDate, endDate);
            LocalDate fromDate = dateRange[0];
            LocalDate toDate = dateRange[1];
            
            // Mock visits data (in real scenario would come from analytics service)
            Long visits = 1000L + (long)(Math.random() * 5000);
            
            // Calculate total orders for conversion rate
            Query ordersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT o.id) FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3");
            ordersQuery.setParameter(1, sellerId);
            ordersQuery.setParameter(2, fromDate);
            ordersQuery.setParameter(3, toDate);
            Long totalOrders = ((Number) ordersQuery.getSingleResult()).longValue();
            
            // Calculate Conversion Rate
            BigDecimal conversionRate = visits > 0 ? 
                new BigDecimal(totalOrders * 100.0 / visits).setScale(2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            // Calculate Returning Customers Rate
            Query returningQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT o1.user_id) FROM orders o1 " +
                "JOIN order_items oi1 ON o1.id = oi1.order_id " +
                "JOIN products p1 ON oi1.product_id = p1.id " +
                "WHERE p1.seller_id = ?1 AND DATE(o1.created_at) BETWEEN ?2 AND ?3 " +
                "AND EXISTS (SELECT 1 FROM orders o2 " +
                "            JOIN order_items oi2 ON o2.id = oi2.order_id " +
                "            JOIN products p2 ON oi2.product_id = p2.id " +
                "            WHERE p2.seller_id = ?1 AND o2.user_id = o1.user_id AND o2.created_at < o1.created_at)");
            returningQuery.setParameter(1, sellerId);
            returningQuery.setParameter(2, fromDate);
            returningQuery.setParameter(3, toDate);
            Long returningCustomers = ((Number) returningQuery.getSingleResult()).longValue();
            
            BigDecimal returningCustomersRate = totalOrders > 0 ? 
                new BigDecimal(returningCustomers * 100.0 / totalOrders).setScale(2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            result.put("visits", visits);
            result.put("conversionRate", conversionRate);
            result.put("returningCustomersRate", returningCustomersRate);
            result.put("totalOrders", totalOrders);
            result.put("period", period);
            result.put("dateRange", Map.of("from", fromDate, "to", toDate));
            
        } catch (Exception e) {
            result = createEmptyKPIs();
            result.put("error", "Failed to fetch user performance KPIs");
        }
        
        return result;
    }

    /**
     * So sánh hiệu suất theo kỳ
     */
    public Map<String, Object> getPeriodComparison(String username, String period) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate currentStart, currentEnd, previousStart, previousEnd;
            
            switch (period) {
                case "week":
                    currentStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
                    currentEnd = LocalDate.now();
                    previousStart = currentStart.minusWeeks(1);
                    previousEnd = currentStart.minusDays(1);
                    break;
                case "month":
                    currentStart = LocalDate.now().withDayOfMonth(1);
                    currentEnd = LocalDate.now();
                    previousStart = currentStart.minusMonths(1);
                    previousEnd = currentStart.minusDays(1);
                    break;
                case "quarter":
                    int currentQuarter = (LocalDate.now().getMonthValue() - 1) / 3;
                    currentStart = LocalDate.now().withMonth(currentQuarter * 3 + 1).withDayOfMonth(1);
                    currentEnd = LocalDate.now();
                    previousStart = currentStart.minusMonths(3);
                    previousEnd = currentStart.minusDays(1);
                    break;
                default: // year
                    currentStart = LocalDate.now().withDayOfYear(1);
                    currentEnd = LocalDate.now();
                    previousStart = currentStart.minusYears(1);
                    previousEnd = currentStart.minusDays(1);
            }
            
            Map<String, Object> currentData = getSalesPerformanceKPIs(username, period, currentStart, currentEnd);
            Map<String, Object> previousData = getSalesPerformanceKPIs(username, period, previousStart, previousEnd);
            
            result.put("current", currentData);
            result.put("previous", previousData);
            result.put("period", period);
            result.put("comparison", calculateDetailedComparison(currentData, previousData));
            
        } catch (Exception e) {
            result.put("error", "Failed to generate period comparison");
        }
        
        return result;
    }

    // ===============================
    // II. BIỂU ĐỒ XU HƯỚNG (Trend Charts)
    // ===============================

    /**
     * Biểu đồ doanh thu theo thời gian
     */
    public ChartDataDTO getRevenueChart(String username, String timeRange, int periods) {
        List<ChartDataDTO.ChartPointDTO> dataPoints = new ArrayList<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return new ChartDataDTO("Revenue Chart", "line", dataPoints, timeRange);
            }
            
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate date = getDateForPeriod(timeRange, i);
                LocalDate startDate = getStartDateForPeriod(timeRange, date);
                LocalDate endDate = getEndDateForPeriod(timeRange, date);
                
                Query query = entityManager.createNativeQuery(
                    "SELECT COALESCE(SUM(oi.price * oi.quantity), 0) FROM order_items oi " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "JOIN orders o ON oi.order_id = o.id " +
                    "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3 " +
                    "AND o.status IN ('completed', 'delivered')");
                query.setParameter(1, sellerId);
                query.setParameter(2, startDate);
                query.setParameter(3, endDate);
                
                BigDecimal revenue = (BigDecimal) query.getSingleResult();
                
                dataPoints.add(new ChartDataDTO.ChartPointDTO(
                    formatDateLabel(date, timeRange),
                    revenue.doubleValue(),
                    date.atStartOfDay()
                ));
            }
        } catch (Exception e) {
            // Return empty chart on error
        }
        
        return new ChartDataDTO("Revenue Chart", "line", dataPoints, timeRange);
    }

    /**
     * Biểu đồ số đơn hàng theo thời gian
     */
    public ChartDataDTO getOrdersChart(String username, String timeRange, int periods) {
        List<ChartDataDTO.ChartPointDTO> dataPoints = new ArrayList<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return new ChartDataDTO("Orders Chart", "line", dataPoints, timeRange);
            }
            
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate date = getDateForPeriod(timeRange, i);
                LocalDate startDate = getStartDateForPeriod(timeRange, date);
                LocalDate endDate = getEndDateForPeriod(timeRange, date);
                
                Query query = entityManager.createNativeQuery(
                    "SELECT COUNT(DISTINCT o.id) FROM orders o " +
                    "JOIN order_items oi ON o.id = oi.order_id " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3");
                query.setParameter(1, sellerId);
                query.setParameter(2, startDate);
                query.setParameter(3, endDate);
                
                Long orderCount = ((Number) query.getSingleResult()).longValue();
                
                dataPoints.add(new ChartDataDTO.ChartPointDTO(
                    formatDateLabel(date, timeRange),
                    orderCount.doubleValue(),
                    date.atStartOfDay()
                ));
            }
        } catch (Exception e) {
            // Return empty chart on error
        }
        
        return new ChartDataDTO("Orders Chart", "line", dataPoints, timeRange);
    }

    /**
     * Biểu đồ giá trị đơn hàng trung bình (AOV)
     */
    public ChartDataDTO getAOVChart(String username, String timeRange, int periods) {
        List<ChartDataDTO.ChartPointDTO> dataPoints = new ArrayList<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return new ChartDataDTO("AOV Chart", "line", dataPoints, timeRange);
            }
            
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate date = getDateForPeriod(timeRange, i);
                LocalDate startDate = getStartDateForPeriod(timeRange, date);
                LocalDate endDate = getEndDateForPeriod(timeRange, date);
                
                // Get revenue and order count for AOV calculation
                Query revenueQuery = entityManager.createNativeQuery(
                    "SELECT COALESCE(SUM(oi.price * oi.quantity), 0), COUNT(DISTINCT o.id) FROM order_items oi " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "JOIN orders o ON oi.order_id = o.id " +
                    "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3 " +
                    "AND o.status IN ('completed', 'delivered')");
                revenueQuery.setParameter(1, sellerId);
                revenueQuery.setParameter(2, startDate);
                revenueQuery.setParameter(3, endDate);
                
                Object[] result = (Object[]) revenueQuery.getSingleResult();
                BigDecimal revenue = (BigDecimal) result[0];
                Long orderCount = ((Number) result[1]).longValue();
                
                Double aov = orderCount > 0 ? revenue.doubleValue() / orderCount : 0.0;
                
                dataPoints.add(new ChartDataDTO.ChartPointDTO(
                    formatDateLabel(date, timeRange),
                    aov,
                    date.atStartOfDay()
                ));
            }
        } catch (Exception e) {
            // Return empty chart on error
        }
        
        return new ChartDataDTO("AOV Chart", "line", dataPoints, timeRange);
    }

    /**
     * Biểu đồ đơn đổi trả hàng
     */
    public ChartDataDTO getReturnsChart(String username, String timeRange, int periods) {
        List<ChartDataDTO.ChartPointDTO> dataPoints = new ArrayList<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return new ChartDataDTO("Returns Chart", "line", dataPoints, timeRange);
            }
            
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate date = getDateForPeriod(timeRange, i);
                LocalDate startDate = getStartDateForPeriod(timeRange, date);
                LocalDate endDate = getEndDateForPeriod(timeRange, date);                
                // Return count - Table removed, set to 0
                Long returnCount = 0L;
                
                dataPoints.add(new ChartDataDTO.ChartPointDTO(
                    formatDateLabel(date, timeRange),
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
     * Biểu đồ lượt truy cập gian hàng
     */
    public ChartDataDTO getVisitsChart(String username, String timeRange, int periods) {
        List<ChartDataDTO.ChartPointDTO> dataPoints = new ArrayList<>();
        
        try {
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate date = getDateForPeriod(timeRange, i);
                
                // Mock visits data (in real scenario would come from analytics service)
                Double visits = 50.0 + (Math.random() * 200.0);
                
                dataPoints.add(new ChartDataDTO.ChartPointDTO(
                    formatDateLabel(date, timeRange),
                    visits,
                    date.atStartOfDay()
                ));
            }
        } catch (Exception e) {
            // Return empty chart on error
        }
        
        return new ChartDataDTO("Visits Chart", "line", dataPoints, timeRange);
    }

    /**
     * Biểu đồ các chỉ số chuyển đổi (CR, RR, PDR)
     */
    public Map<String, ChartDataDTO> getConversionMetricsCharts(String username, String timeRange, int periods) {
        Map<String, ChartDataDTO> result = new HashMap<>();
        
        List<ChartDataDTO.ChartPointDTO> conversionPoints = new ArrayList<>();
        List<ChartDataDTO.ChartPointDTO> returnRatePoints = new ArrayList<>();
        List<ChartDataDTO.ChartPointDTO> retentionPoints = new ArrayList<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate date = getDateForPeriod(timeRange, i);
                String label = formatDateLabel(date, timeRange);
                
                // Mock conversion rate data
                Double conversionRate = 2.0 + (Math.random() * 3.0);
                conversionPoints.add(new ChartDataDTO.ChartPointDTO(label, conversionRate, date.atStartOfDay()));
                
                // Mock return rate data
                Double returnRate = 1.0 + (Math.random() * 2.0);
                returnRatePoints.add(new ChartDataDTO.ChartPointDTO(label, returnRate, date.atStartOfDay()));
                
                // Mock retention rate data
                Double retentionRate = 15.0 + (Math.random() * 20.0);
                retentionPoints.add(new ChartDataDTO.ChartPointDTO(label, retentionRate, date.atStartOfDay()));
            }
        } catch (Exception e) {
            // Return empty charts on error
        }
        
        result.put("conversionRate", new ChartDataDTO("Conversion Rate", "line", conversionPoints, timeRange));
        result.put("returnRate", new ChartDataDTO("Return Rate", "line", returnRatePoints, timeRange));
        result.put("retentionRate", new ChartDataDTO("Customer Retention Rate", "line", retentionPoints, timeRange));
        
        return result;
    }

    // ===============================
    // III. DỰ ĐOÁN ARIMA (ARIMA Predictions)
    // ===============================

    /**
     * Dự đoán ARIMA tổng hợp
     */
    public Map<String, Object> getARIMAPredictions(String username, int forecastMonths, int historicalMonths) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return createEmptyPredictions();
            }
            
            // Historical data
            List<Map<String, Object>> historicalRevenue = getHistoricalData(sellerId, "revenue", historicalMonths);
            List<Map<String, Object>> historicalOrders = getHistoricalData(sellerId, "orders", historicalMonths);
            List<Map<String, Object>> historicalCustomers = getHistoricalData(sellerId, "customers", historicalMonths);
            
            // Forecast data (simple implementation)
            List<Map<String, Object>> forecastRevenue = generateForecastData(historicalRevenue, forecastMonths);
            List<Map<String, Object>> forecastOrders = generateForecastData(historicalOrders, forecastMonths);
            List<Map<String, Object>> forecastCustomers = generateForecastData(historicalCustomers, forecastMonths);
            
            Map<String, Object> revenueData = new HashMap<>();
            revenueData.put("historical", historicalRevenue);
            revenueData.put("forecast", forecastRevenue);
            
            Map<String, Object> ordersData = new HashMap<>();
            ordersData.put("historical", historicalOrders);
            ordersData.put("forecast", forecastOrders);
            
            Map<String, Object> customersData = new HashMap<>();
            customersData.put("historical", historicalCustomers);
            customersData.put("forecast", forecastCustomers);
            
            result.put("revenue", revenueData);
            result.put("orders", ordersData);
            result.put("customers", customersData);
            result.put("forecastMonths", forecastMonths);
            result.put("historicalMonths", historicalMonths);
            
        } catch (Exception e) {
            result.put("error", "Failed to generate ARIMA predictions");
        }
        
        return result;
    }

    /**
     * Dự đoán doanh thu
     */
    public Map<String, Object> getRevenuePredictions(String username, int forecastMonths) {
        return getSpecificPrediction(username, "revenue", forecastMonths);
    }

    /**
     * Dự đoán số đơn hàng
     */
    public Map<String, Object> getOrdersPredictions(String username, int forecastMonths) {
        return getSpecificPrediction(username, "orders", forecastMonths);
    }

    /**
     * Dự đoán số khách hàng
     */
    public Map<String, Object> getCustomersPredictions(String username, int forecastMonths) {
        return getSpecificPrediction(username, "customers", forecastMonths);
    }

    // ===============================
    // IV. TIỆN ÍCH & BỘ LỌC (Utilities & Filters)
    // ===============================

    /**
     * Thống kê nhanh
     */
    public Map<String, Object> getQuickStats(String username) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return createEmptyStats();
            }
            
            LocalDate today = LocalDate.now();
            
            // Today's orders
            Query todayOrdersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT o.id), COALESCE(SUM(oi.price * oi.quantity), 0) FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE p.seller_id = ?1 AND DATE(o.created_at) = ?2");
            todayOrdersQuery.setParameter(1, sellerId);
            todayOrdersQuery.setParameter(2, today);
            Object[] todayData = (Object[]) todayOrdersQuery.getSingleResult();
            
            // This month's stats
            LocalDate monthStart = today.withDayOfMonth(1);
            Query monthQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT o.id), COALESCE(SUM(oi.price * oi.quantity), 0) FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3");
            monthQuery.setParameter(1, sellerId);
            monthQuery.setParameter(2, monthStart);
            monthQuery.setParameter(3, today);
            Object[] monthData = (Object[]) monthQuery.getSingleResult();
            
            result.put("todayOrders", todayData[0]);
            result.put("todayRevenue", todayData[1]);
            result.put("monthOrders", monthData[0]);
            result.put("monthRevenue", monthData[1]);
            result.put("lastUpdated", LocalDateTime.now());
            
        } catch (Exception e) {
            result = createEmptyStats();
            result.put("error", "Failed to get quick stats");
        }
        
        return result;
    }

    /**
     * Lấy các khoảng thời gian định sẵn
     */
    public Map<String, Object> getPredefinedDateRanges() {
        Map<String, Object> ranges = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        
        ranges.put("7_days", Map.of(
            "label", "7 ngày qua",
            "startDate", today.minusDays(7),
            "endDate", today
        ));
        
        ranges.put("30_days", Map.of(
            "label", "30 ngày qua",
            "startDate", today.minusDays(30),
            "endDate", today
        ));
        
        ranges.put("this_week", Map.of(
            "label", "Tuần này",
            "startDate", today.minusDays(today.getDayOfWeek().getValue() - 1),
            "endDate", today
        ));
        
        ranges.put("last_week", Map.of(
            "label", "Tuần trước",
            "startDate", today.minusDays(today.getDayOfWeek().getValue() + 6),
            "endDate", today.minusDays(today.getDayOfWeek().getValue())
        ));
        
        ranges.put("this_month", Map.of(
            "label", "Tháng này",
            "startDate", today.withDayOfMonth(1),
            "endDate", today
        ));
        
        ranges.put("last_month", Map.of(
            "label", "Tháng trước",
            "startDate", today.minusMonths(1).withDayOfMonth(1),
            "endDate", today.withDayOfMonth(1).minusDays(1)
        ));
        
        int currentQuarter = (today.getMonthValue() - 1) / 3;
        ranges.put("this_quarter", Map.of(
            "label", "Quý này",
            "startDate", today.withMonth(currentQuarter * 3 + 1).withDayOfMonth(1),
            "endDate", today
        ));
        
        ranges.put("last_quarter", Map.of(
            "label", "Quý trước",
            "startDate", today.withMonth(currentQuarter * 3 + 1).withDayOfMonth(1).minusMonths(3),
            "endDate", today.withMonth(currentQuarter * 3 + 1).withDayOfMonth(1).minusDays(1)
        ));
        
        return ranges;
    }

    /**
     * Hiệu suất sản phẩm
     */
    public Map<String, Object> getProductsPerformance(String username, String period, int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return Map.of("products", new ArrayList<>());
            }
            
            LocalDate[] dateRange = calculateDateRange(period, null, null);
            
            Query query = entityManager.createNativeQuery(
                "SELECT p.name, p.id, " +
                "COUNT(DISTINCT oi.order_id) as order_count, " +
                "SUM(oi.quantity) as total_sold, " +
                "SUM(oi.price * oi.quantity) as revenue " +
                "FROM products p " +
                "LEFT JOIN order_items oi ON p.id = oi.product_id " +
                "LEFT JOIN orders o ON oi.order_id = o.id " +
                "WHERE p.seller_id = ?1 " +
                "AND (o.created_at IS NULL OR DATE(o.created_at) BETWEEN ?2 AND ?3) " +
                "GROUP BY p.id, p.name " +
                "ORDER BY revenue DESC LIMIT ?4");
            query.setParameter(1, sellerId);
            query.setParameter(2, dateRange[0]);
            query.setParameter(3, dateRange[1]);
            query.setParameter(4, limit);
            
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> products = new ArrayList<>();
            
            for (Object[] row : results) {
                Map<String, Object> product = new HashMap<>();
                product.put("name", row[0]);
                product.put("id", row[1]);
                product.put("orderCount", row[2] != null ? row[2] : 0);
                product.put("totalSold", row[3] != null ? row[3] : 0);
                product.put("revenue", row[4] != null ? row[4] : BigDecimal.ZERO);
                products.add(product);
            }
            
            result.put("products", products);
            result.put("period", period);
            result.put("limit", limit);
            
        } catch (Exception e) {
            result.put("products", new ArrayList<>());
            result.put("error", "Failed to get products performance");
        }
        
        return result;
    }

    /**
     * Export dữ liệu dashboard
     */
    public byte[] exportDashboardData(String username, String format, LocalDate startDate, LocalDate endDate) {
        try {
            // Mock implementation
            String content = "Seller Dashboard Export\n";
            content += "Seller: " + username + "\n";
            content += "Format: " + format + "\n";
            content += "Date Range: " + startDate + " to " + endDate + "\n";
            content += "Generated at: " + LocalDateTime.now() + "\n";
            
            // Add KPI data
            Map<String, Object> kpis = getSellerKPIs(username, "custom", startDate, endDate);
            content += "\nKPIs Summary:\n" + kpis.toString();
            
            return content.getBytes();
            
        } catch (Exception e) {
            return "Export failed".getBytes();
        }
    }

    // ===============================
    // HELPER METHODS
    // ===============================

    private Long getSellerIdByUsername(String username) {
        try {
            Query query = entityManager.createNativeQuery(
                "SELECT id FROM users WHERE username = ?1 AND role = 'SELLER'");
            query.setParameter(1, username);
            return ((Number) query.getSingleResult()).longValue();
        } catch (Exception e) {
            return null;
        }
    }    private LocalDate[] calculateDateRange(String period, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            // Validate minimum 7 days range for custom dates
            long daysBetween = endDate.toEpochDay() - startDate.toEpochDay();
            if (daysBetween < 6) { // minimum 7 days (0-6 = 7 days)
                endDate = startDate.plusDays(6);
            }
            return new LocalDate[]{startDate, endDate};
        }
        
        LocalDate today = LocalDate.now();
        
        switch (period) {
            case "7_days":
                return new LocalDate[]{today.minusDays(6), today}; // 7 days total
            case "30_days":
                return new LocalDate[]{today.minusDays(29), today}; // 30 days total
            case "this_week":
                return new LocalDate[]{today.minusDays(today.getDayOfWeek().getValue() - 1), today};
            case "last_week":
                LocalDate lastWeekStart = today.minusDays(today.getDayOfWeek().getValue() + 6);
                return new LocalDate[]{lastWeekStart, lastWeekStart.plusDays(6)};
            case "this_month":
                return new LocalDate[]{today.withDayOfMonth(1), today};
            case "last_month":
                LocalDate lastMonthStart = today.minusMonths(1).withDayOfMonth(1);
                return new LocalDate[]{lastMonthStart, lastMonthStart.withDayOfMonth(lastMonthStart.lengthOfMonth())};
            case "this_quarter":
                int currentQuarter = (today.getMonthValue() - 1) / 3;
                return new LocalDate[]{today.withMonth(currentQuarter * 3 + 1).withDayOfMonth(1), today};
            case "last_quarter":
                int prevQuarter = (today.getMonthValue() - 1) / 3;
                LocalDate quarterStart = today.withMonth(prevQuarter * 3 + 1).withDayOfMonth(1).minusMonths(3);
                return new LocalDate[]{quarterStart, quarterStart.plusMonths(3).minusDays(1)};
            default:
                return new LocalDate[]{today.minusDays(6), today}; // Default to 7 days
        }
    }

    private LocalDate[] calculatePreviousPeriodRange(String period, LocalDate fromDate, LocalDate toDate) {
        long daysBetween = toDate.toEpochDay() - fromDate.toEpochDay();
        return new LocalDate[]{
            fromDate.minusDays(daysBetween + 1),
            fromDate.minusDays(1)
        };
    }

    private Map<String, Object> getPreviousPeriodKPIs(Long sellerId, LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Previous period revenue
            Query revenueQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(oi.price * oi.quantity), 0) FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3 " +
                "AND o.status IN ('completed', 'delivered')");
            revenueQuery.setParameter(1, sellerId);
            revenueQuery.setParameter(2, fromDate);
            revenueQuery.setParameter(3, toDate);
            result.put("revenue", revenueQuery.getSingleResult());
            
            // Previous period orders
            Query ordersQuery = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT o.id) FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3");
            ordersQuery.setParameter(1, sellerId);
            ordersQuery.setParameter(2, fromDate);
            ordersQuery.setParameter(3, toDate);
            result.put("totalOrders", ordersQuery.getSingleResult());
            
        } catch (Exception e) {
            result.put("revenue", BigDecimal.ZERO);
            result.put("totalOrders", 0L);
        }
        
        return result;
    }

    private Map<String, Object> calculateGrowthRates(Map<String, Object> current, Map<String, Object> previous) {
        Map<String, Object> growth = new HashMap<>();
        
        try {
            BigDecimal currentRevenue = (BigDecimal) current.get("revenue");
            BigDecimal previousRevenue = (BigDecimal) previous.get("revenue");
            
            if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal revenueGrowth = currentRevenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
                growth.put("revenueGrowth", revenueGrowth);
            } else {
                growth.put("revenueGrowth", BigDecimal.ZERO);
            }
            
            Long currentOrders = (Long) current.get("totalOrders");
            Long previousOrders = (Long) previous.get("totalOrders");
            
            if (previousOrders > 0) {
                Double ordersGrowth = ((currentOrders - previousOrders) * 100.0) / previousOrders;
                growth.put("ordersGrowth", ordersGrowth);
            } else {
                growth.put("ordersGrowth", 0.0);
            }
            
        } catch (Exception e) {
            growth.put("revenueGrowth", BigDecimal.ZERO);
            growth.put("ordersGrowth", 0.0);
        }
        
        return growth;
    }

    private Map<String, Object> calculateDetailedComparison(Map<String, Object> current, Map<String, Object> previous) {
        Map<String, Object> comparison = new HashMap<>();
        
        try {
            // Revenue comparison
            BigDecimal currentRevenue = (BigDecimal) current.get("revenue");
            BigDecimal previousRevenue = (BigDecimal) previous.get("revenue");
            comparison.put("revenueDifference", currentRevenue.subtract(previousRevenue));
            
            // Orders comparison
            Long currentOrders = (Long) current.get("totalOrders");
            Long previousOrders = (Long) previous.get("totalOrders");
            comparison.put("ordersDifference", currentOrders - previousOrders);
            
            // Growth calculations
            comparison.putAll(calculateGrowthRates(current, previous));
            
        } catch (Exception e) {
            comparison.put("error", "Failed to calculate comparison");
        }
        
        return comparison;
    }    private LocalDate getDateForPeriod(String timeRange, int periodsBack) {
        switch (timeRange) {
            case "weekly":
                return LocalDate.now().minusWeeks(periodsBack);
            case "monthly":
                return LocalDate.now().minusMonths(periodsBack);
            default: // fallback to weekly for any invalid timeRange
                return LocalDate.now().minusWeeks(periodsBack);
        }
    }

    private LocalDate getStartDateForPeriod(String timeRange, LocalDate date) {
        switch (timeRange) {
            case "weekly":
                return date.minusDays(date.getDayOfWeek().getValue() - 1);
            case "monthly":
                return date.withDayOfMonth(1);
            default: // fallback to weekly
                return date.minusDays(date.getDayOfWeek().getValue() - 1);
        }
    }

    private LocalDate getEndDateForPeriod(String timeRange, LocalDate date) {
        switch (timeRange) {
            case "weekly":
                return date.plusDays(7 - date.getDayOfWeek().getValue());
            case "monthly":
                return date.withDayOfMonth(date.lengthOfMonth());
            default: // fallback to weekly
                return date.plusDays(7 - date.getDayOfWeek().getValue());
        }
    }

    private String formatDateLabel(LocalDate date, String timeRange) {
        switch (timeRange) {
            case "weekly":
                return "W" + date.format(DateTimeFormatter.ofPattern("w/yyyy"));
            case "monthly":
                return date.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            default: // fallback to weekly
                return "W" + date.format(DateTimeFormatter.ofPattern("w/yyyy"));
        }
    }

    private List<Map<String, Object>> getHistoricalData(Long sellerId, String type, int months) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            LocalDate startDate = date.withDayOfMonth(1);
            LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());
            
            Map<String, Object> item = new HashMap<>();
            item.put("month", date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            
            try {
                switch (type) {
                    case "revenue":
                        Query revenueQuery = entityManager.createNativeQuery(
                            "SELECT COALESCE(SUM(oi.price * oi.quantity), 0) FROM order_items oi " +
                            "JOIN products p ON oi.product_id = p.id " +
                            "JOIN orders o ON oi.order_id = o.id " +
                            "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3 " +
                            "AND o.status IN ('completed', 'delivered')");
                        revenueQuery.setParameter(1, sellerId);
                        revenueQuery.setParameter(2, startDate);
                        revenueQuery.setParameter(3, endDate);
                        BigDecimal revenue = (BigDecimal) revenueQuery.getSingleResult();
                        item.put("value", revenue.doubleValue());
                        break;
                        
                    case "orders":
                        Query ordersQuery = entityManager.createNativeQuery(
                            "SELECT COUNT(DISTINCT o.id) FROM orders o " +
                            "JOIN order_items oi ON o.id = oi.order_id " +
                            "JOIN products p ON oi.product_id = p.id " +
                            "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3");
                        ordersQuery.setParameter(1, sellerId);
                        ordersQuery.setParameter(2, startDate);
                        ordersQuery.setParameter(3, endDate);
                        Long orders = ((Number) ordersQuery.getSingleResult()).longValue();
                        item.put("value", orders.doubleValue());
                        break;
                        
                    case "customers":
                        Query customersQuery = entityManager.createNativeQuery(
                            "SELECT COUNT(DISTINCT o.user_id) FROM orders o " +
                            "JOIN order_items oi ON o.id = oi.order_id " +
                            "JOIN products p ON oi.product_id = p.id " +
                            "WHERE p.seller_id = ?1 AND DATE(o.created_at) BETWEEN ?2 AND ?3");
                        customersQuery.setParameter(1, sellerId);
                        customersQuery.setParameter(2, startDate);
                        customersQuery.setParameter(3, endDate);
                        Long customers = ((Number) customersQuery.getSingleResult()).longValue();
                        item.put("value", customers.doubleValue());
                        break;
                        
                    default:
                        item.put("value", 0);
                }
            } catch (Exception e) {
                item.put("value", 0);
            }
            
            result.add(item);
        }
        
        return result;
    }

    private List<Map<String, Object>> generateForecastData(List<Map<String, Object>> historical, int months) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (historical.isEmpty()) return result;
        
        // Simple trend calculation
        Map<String, Object> lastPoint = historical.get(historical.size() - 1);
        double lastValue = ((Number) lastPoint.get("value")).doubleValue();
        
        for (int i = 1; i <= months; i++) {
            LocalDate futureDate = LocalDate.now().plusMonths(i);
            Map<String, Object> item = new HashMap<>();
            item.put("month", futureDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            
            // Simple forecast with trend and randomness
            double forecast = lastValue * (1.0 + 0.05 + (Math.random() * 0.1 - 0.05));
            item.put("value", Math.round(forecast));
            item.put("forecast", true);
            
            result.add(item);
            lastValue = forecast;
        }
        
        return result;
    }

    private Map<String, Object> getSpecificPrediction(String username, String type, int forecastMonths) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long sellerId = getSellerIdByUsername(username);
            if (sellerId == null) {
                return createEmptyPredictions();
            }
            
            List<Map<String, Object>> historical = getHistoricalData(sellerId, type, 9);
            List<Map<String, Object>> forecast = generateForecastData(historical, forecastMonths);
            
            result.put("type", type);
            result.put("historical", historical);
            result.put("forecast", forecast);
            result.put("forecastMonths", forecastMonths);
            
        } catch (Exception e) {
            result.put("error", "Failed to generate " + type + " predictions");
        }
        
        return result;
    }

    private Map<String, Object> createEmptyKPIs() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("revenue", BigDecimal.ZERO);
        empty.put("totalOrders", 0L);
        empty.put("aov", BigDecimal.ZERO);
        empty.put("returnsCount", 0L);
        empty.put("visits", 0L);
        empty.put("conversionRate", BigDecimal.ZERO);
        empty.put("returningCustomersRate", BigDecimal.ZERO);
        return empty;
    }

    private Map<String, Object> createEmptyStats() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("todayOrders", 0L);
        empty.put("todayRevenue", BigDecimal.ZERO);
        empty.put("monthOrders", 0L);
        empty.put("monthRevenue", BigDecimal.ZERO);
        return empty;
    }

    private Map<String, Object> createEmptyPredictions() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("historical", new ArrayList<>());
        empty.put("forecast", new ArrayList<>());
        return empty;
    }
}
