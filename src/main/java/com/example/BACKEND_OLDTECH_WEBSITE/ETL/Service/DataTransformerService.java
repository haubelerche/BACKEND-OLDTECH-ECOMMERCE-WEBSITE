package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin.ChartDataDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository.SellerDashboardRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service chuyển đổi dữ liệu thô thành dữ liệu phân tích business
 * Xử lý logic kinh doanh cho thị trường đồ công nghệ cũ
 * CHUYÊN: Laptop, Điện thoại, PC, Tai nghe, Loa, Camera, Tablet, Đồng hồ thông minh, Phụ kiện công nghệ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataTransformerService {

    /**
     * Chuyển đổi dữ liệu thô từ DB thành dữ liệu phân tích
     */
    public TransformedData transform(ExtractedData extractedData) {
        log.info("Bắt đầu chuyển đổi dữ liệu vào ngày: {}", extractedData.getExtractionDate());
        
        try {
            TransformedData transformedData = new TransformedData();
            transformedData.setProcessDate(extractedData.getExtractionDate());

            // Bước 1: Tính toán metrics bán hàng (doanh thu, đơn hàng)
            transformedData.setSalesMetrics(transformSalesMetrics(extractedData));
            log.info("Chuyển đổi thống kê bán hàng hoàn tất");

            // Bước 2: Phân khúc khách hàng theo hành vi mua sắm
            transformedData.setCustomerSegments(calculateCustomerSegments(extractedData));
            log.info("Phân tích khách hàng hoàn tất");

            // Bước 3: Phân tích hiệu suất từng sản phẩm
            transformedData.setProductAnalytics(analyzeProductPerformance(extractedData));
            log.info("Phân tích hiệu suất sản phẩm hoàn tất");

            // Bước 4: Tính các chỉ số KPI kinh doanh
            transformedData.setBusinessKPIs(calculateBusinessKPIs(extractedData, transformedData));
            log.info("Tính toán chỉ số kinh doanh hoàn tất");

            // Bước 5: Kiểm tra và cảnh báo chất lượng dữ liệu
            transformedData.setDataQualityAlerts(generateDataQualityAlerts(extractedData));
            log.info("Tạo cảnh báo chất lượng dữ liệu hoàn tất");

            log.info("Chuyển đổi dữ liệu hoàn tất thành công");
            return transformedData;

        } catch (Exception e) {
            log.error("Lỗi trong quá trình chuyển đổi dữ liệu", e);
            throw new RuntimeException("Chuyển đổi dữ liệu thất bại", e);
        }
    }    /**
     * Chuyển đổi dữ liệu đơn hàng thành các chỉ số bán hàng
     */
    private TransformedData.SalesMetrics transformSalesMetrics(ExtractedData data) {
        TransformedData.SalesMetrics metrics = new TransformedData.SalesMetrics();
        
        List<ExtractedData.OrderData> orders = data.getOrders();
        
        // Tính tổng doanh thu từ tất cả đơn hàng
        BigDecimal totalRevenue = orders.stream()
                .map(ExtractedData.OrderData::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Đếm tổng số đơn hàng
        int totalOrders = orders.size();
        
        // Đếm số khách hàng unique (không trùng lặp)
        long uniqueCustomers = orders.stream()
                .map(ExtractedData.OrderData::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        
        // Tính giá trị đơn hàng trung bình (AOV - Average Order Value)
        // Đối với thị trường đồ cũ: mỗi đơn hàng = 1 sản phẩm unique
        BigDecimal averageOrderValue = totalOrders > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        // Phân tích doanh thu theo danh mục sản phẩm
        Map<String, BigDecimal> revenueByCategory = orders.stream()
                .filter(order -> order.getProductCategory() != null && order.getTotalAmount() != null)
                .collect(Collectors.groupingBy(
                        ExtractedData.OrderData::getProductCategory,
                        Collectors.reducing(BigDecimal.ZERO, 
                                ExtractedData.OrderData::getTotalAmount, 
                                BigDecimal::add)
                ));
          // Phân tích số lượng đơn hàng theo trạng thái
        Map<String, Integer> itemsByStatus = orders.stream()
                .filter(order -> order.getStatus() != null)
                .collect(Collectors.groupingBy(
                        ExtractedData.OrderData::getStatus,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
        
        // Tách đơn hàng thành công vs bị hủy
        int soldItems = itemsByStatus.getOrDefault("COMPLETED", 0);     
        int cancelledItems = itemsByStatus.getOrDefault("CANCELLED", 0);
        
        // Tính số tiền hoàn trả (giả định 2% cho thị trường đồ cũ)
        BigDecimal refundAmount = totalRevenue.multiply(BigDecimal.valueOf(0.02));
        
        // Tính tốc độ bán hàng trên marketplace (sell-through rate)
        BigDecimal marketplaceVelocity = totalOrders > 0 ? 
                BigDecimal.valueOf(soldItems).divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;        
        // Gán kết quả vào metrics object
        metrics.setTotalRevenue(totalRevenue);
        metrics.setTotalOrders(totalOrders);
        metrics.setUniqueCustomers((int) uniqueCustomers);
        metrics.setAverageOrderValue(averageOrderValue);
        metrics.setRevenueByCategory(revenueByCategory);
        metrics.setOrdersByStatus(itemsByStatus);
        metrics.setGrowthRate(marketplaceVelocity); // Tái sử dụng field này cho tốc độ bán
        metrics.setCancelledOrders(cancelledItems);
        metrics.setRefundAmount(refundAmount);
        
        return metrics;
    }

    /**
     * Phân khúc khách hàng dựa trên hành vi mua sắm trong thị trường đồ cũ
     */
    private TransformedData.CustomerSegments calculateCustomerSegments(ExtractedData data) {
        TransformedData.CustomerSegments segments = new TransformedData.CustomerSegments();
        
        // Nhóm các đơn hàng theo từng khách hàng để phân tích hành vi
        Map<Integer, List<ExtractedData.OrderData>> ordersByCustomer = data.getOrders().stream()
                .filter(order -> order.getUserId() != null)
                .collect(Collectors.groupingBy(ExtractedData.OrderData::getUserId));
        
        List<TransformedData.CustomerSegments.Segment> segmentList = new ArrayList<>();
          // Khởi tạo các biến đếm cho 3 nhóm khách hàng chính
          // Nhóm 1: Khách mua đồ công nghệ lần đầu (first-time tech buyers)
        int firstTimeBuyers = 0;
        BigDecimal firstTimeBuyersSpend = BigDecimal.ZERO;
        
        // Nhóm 2: Tech enthusiasts (người yêu công nghệ - mua nhiều items)
        int techEnthusiasts = 0;
        BigDecimal techEnthusiastsSpend = BigDecimal.ZERO;
        
        // Nhóm 3: Budget tech buyers (người tìm công nghệ giá rẻ)
        int budgetTechBuyers = 0;
        BigDecimal budgetTechBuyersSpend = BigDecimal.ZERO;
        
        // Duyệt qua từng khách hàng để phân loại
        for (Map.Entry<Integer, List<ExtractedData.OrderData>> entry : ordersByCustomer.entrySet()) {
            List<ExtractedData.OrderData> customerItems = entry.getValue();
            
            // Tính tổng tiền khách hàng này đã chi
            BigDecimal customerTotalSpend = customerItems.stream()
                    .map(ExtractedData.OrderData::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
              // Logic phân loại khách hàng theo số lượng đơn hàng đồ công nghệ
            if (customerItems.size() == 1) {
                // Khách mua đồ công nghệ lần đầu
                firstTimeBuyers++;
                firstTimeBuyersSpend = firstTimeBuyersSpend.add(customerTotalSpend);
            } else if (customerItems.size() >= 5) {
                // Tech enthusiast - mua từ 5 items công nghệ trở lên
                techEnthusiasts++;
                techEnthusiastsSpend = techEnthusiastsSpend.add(customerTotalSpend);
            } else {
                // Budget tech buyer - mua 2-4 items công nghệ
                budgetTechBuyers++;
                budgetTechBuyersSpend = budgetTechBuyersSpend.add(customerTotalSpend);
            }
        }
        
        // Tạo segment cho nhóm khách mua lần đầu
        if (firstTimeBuyers > 0) {
            TransformedData.CustomerSegments.Segment newBuyerSegment = new TransformedData.CustomerSegments.Segment();
            newBuyerSegment.setSegmentName("FIRST_TIME_BUYER");
            newBuyerSegment.setCustomerCount(firstTimeBuyers);
            newBuyerSegment.setAvgOrderValue(firstTimeBuyersSpend.divide(BigDecimal.valueOf(firstTimeBuyers), 2, RoundingMode.HALF_UP));
            newBuyerSegment.setConversionRate(75.0); // Tỷ lệ chuyển đổi thấp hơn cho hàng đã qua sử dụng
            newBuyerSegment.setCharacteristics(Arrays.asList("Người mới", "Nhà tiêu dùng thận trọng", "Nhà đề cao giá trị")); 
            segmentList.add(newBuyerSegment);
        }
          // Tạo segment cho nhóm tech enthusiasts
        if (techEnthusiasts > 0) {
            TransformedData.CustomerSegments.Segment techSegment = new TransformedData.CustomerSegments.Segment();
            techSegment.setSegmentName("TECH_ENTHUSIAST");
            techSegment.setCustomerCount(techEnthusiasts);
            techSegment.setAvgOrderValue(techEnthusiastsSpend.divide(BigDecimal.valueOf(techEnthusiasts), 2, RoundingMode.HALF_UP));
            techSegment.setConversionRate(85.0); // Tỷ lệ chuyển đổi cao cho tech enthusiasts
            techSegment.setCharacteristics(Arrays.asList("Người yêu công nghệ", "Mua nhiều thiết bị", "Ưa thích thương hiệu"));
            segmentList.add(techSegment);
        }
        
        // Tạo segment cho nhóm budget tech buyers
        if (budgetTechBuyers > 0) {            TransformedData.CustomerSegments.Segment budgetSegment = new TransformedData.CustomerSegments.Segment();
            budgetSegment.setSegmentName("BUDGET_TECH_BUYER");
            budgetSegment.setCustomerCount(budgetTechBuyers);
            budgetSegment.setAvgOrderValue(budgetTechBuyersSpend.divide(BigDecimal.valueOf(budgetTechBuyers), 2, RoundingMode.HALF_UP));
            budgetSegment.setConversionRate(60.0); // Tỷ lệ chuyển đổi moderate cho budget buyers
            budgetSegment.setCharacteristics(Arrays.asList("Người nhạy giá", "Tìm kiếm deal tốt", "Mua đồ công nghệ cũ để tiết kiệm"));
            segmentList.add(budgetSegment);
        }
        
        segments.setSegments(segmentList);
          // Retention rates cho thị trường đồ công nghệ cũ
        Map<String, Double> retentionRates = new HashMap<>();
        retentionRates.put("FIRST_TIME_BUYER", 40.0); // Moderate cho first-time tech buyers
        retentionRates.put("TECH_ENTHUSIAST", 85.0); // High cho tech enthusiasts  
        retentionRates.put("BUDGET_TECH_BUYER", 65.0); // Good cho budget buyers
        segments.setRetentionRates(retentionRates);
        
        // Customer lifetime values - đồ công nghệ cũ có CLV cao hơn
        Map<String, BigDecimal> lifetimeValues = new HashMap<>();
        lifetimeValues.put("FIRST_TIME_BUYER", BigDecimal.valueOf(800)); // Cao hơn do AOV cao
        lifetimeValues.put("TECH_ENTHUSIAST", BigDecimal.valueOf(2500)); // Rất cao cho enthusiasts        
        lifetimeValues.put("BUDGET_TECH_BUYER", BigDecimal.valueOf(1200)); // Trung bình cao
        segments.setLifetimeValues(lifetimeValues);
        
        return segments;
    }    /**
     * Phân tích hiệu suất items cho secondhand marketplace
     */
    private TransformedData.ProductAnalytics analyzeProductPerformance(ExtractedData data) {
        TransformedData.ProductAnalytics analytics = new TransformedData.ProductAnalytics();
        
        List<ExtractedData.ProductMetrics> items = data.getProductMetrics();
        
        // Phân tích performance theo từng unique item
        List<TransformedData.ProductAnalytics.ProductPerformance> performances = items.stream()
                .map(item -> {
                    TransformedData.ProductAnalytics.ProductPerformance performance = 
                            new TransformedData.ProductAnalytics.ProductPerformance();
                    performance.setProductId(item.getProductId());
                    performance.setProductName(item.getProductName());
                    performance.setCategory(item.getCategory());
                    performance.setRevenue(item.getRevenue());
                    // Secondhand: mỗi item chỉ bán 1 lần
                    performance.setUnitsSold(item.getPurchaseCount() > 0 ? 1 : 0); 
                    
                    // Conversion rate: views to sale (0 hoặc 1)
                    double conversionRate = item.getViewCount() > 0 ? 
                            (double) performance.getUnitsSold() / item.getViewCount() * 100 : 0.0;
                    performance.setConversionRate(conversionRate);
                    
                    // Margin cho secondhand items (thường cao hơn)
                    performance.setProfitMargin(30.0 + Math.random() * 40); // 30-70% margin
                    
                    return performance;
                })
                .sorted((p1, p2) -> p2.getRevenue().compareTo(p1.getRevenue()))
                .collect(Collectors.toList());
        
        // Items bán chạy nhất (sold fastest)
        analytics.setTopPerformingProducts(performances.stream()
                .filter(p -> p.getUnitsSold() > 0) // Chỉ items đã bán
                .limit(10)
                .collect(Collectors.toList()));
        
        // Items khó bán (nhiều views nhưng chưa bán)
        analytics.setUnderPerformingProducts(performances.stream()
                .filter(p -> p.getUnitsSold() == 0) // Chưa bán
                .sorted((p1, p2) -> Double.compare(p2.getConversionRate(), p1.getConversionRate()))
                .limit(5)
                .collect(Collectors.toList()));
        
        // Category trends cho secondhand marketplace
        Map<String, TransformedData.ProductAnalytics.TrendAnalysis> categoryTrends = new HashMap<>();
        
        // Nhóm items theo category
        Map<String, List<ExtractedData.ProductMetrics>> itemsByCategory = items.stream()
                .collect(Collectors.groupingBy(ExtractedData.ProductMetrics::getCategory));
        
        for (Map.Entry<String, List<ExtractedData.ProductMetrics>> entry : itemsByCategory.entrySet()) {
            String category = entry.getKey();
            List<ExtractedData.ProductMetrics> categoryItems = entry.getValue();
              TransformedData.ProductAnalytics.TrendAnalysis trend = 
                    new TransformedData.ProductAnalytics.TrendAnalysis();
            trend.setCategory(category);
            
            // Secondhand trend analysis based on sell-through rate
            int itemsSold = (int) categoryItems.stream()
                    .mapToInt(ExtractedData.ProductMetrics::getPurchaseCount)
                    .sum();
            int totalItemsListed = categoryItems.size();
            
            // Trend dựa trên sell-through rate
            double sellThroughRate = totalItemsListed > 0 ? 
                    (double) itemsSold / totalItemsListed * 100 : 0.0;
            
            if (sellThroughRate > 70.0) {
                trend.setTrendDirection("HIGH_DEMAND");
                trend.setChangePercentage(sellThroughRate);
            } else if (sellThroughRate > 40.0) {
                trend.setTrendDirection("MODERATE_DEMAND");
                trend.setChangePercentage(sellThroughRate);
            } else {
                trend.setTrendDirection("LOW_DEMAND");
                trend.setChangePercentage(sellThroughRate);
            }              trend.setFactors(Arrays.asList(  "Tình trạng sản phẩm",
            "Chiến lược giá",
            "Nhu cầu thị trường"));
            categoryTrends.put(category, trend);
        }
          analytics.setCategoryTrends(categoryTrends);
        
        // Availability alerts cho secondhand (thay vì stock alerts)
        Map<String, Integer> availabilityAlerts = items.stream()
                .filter(item -> item.getPurchaseCount() == 0) // Items chưa bán
                .collect(Collectors.toMap(
                        item -> item.getProductName(),
                        item -> item.getViewCount() // Views nhưng chưa bán
                ));
        analytics.setStockAlerts(availabilityAlerts); // Reuse field với ý nghĩa mới
        
        return analytics;
    }

    /**
     * tính toán chỉ số kinh doanh
     */
    private TransformedData.BusinessKPIs calculateBusinessKPIs(ExtractedData extractedData, 
                                                               TransformedData transformedData) {
        TransformedData.BusinessKPIs kpis = new TransformedData.BusinessKPIs();
        
        TransformedData.SalesMetrics salesMetrics = transformedData.getSalesMetrics();
        
        // chi phí thu hút khách hàng (simulation)
        double customerAcquisitionCost = 50.0 + Math.random() * 100; // $50-150
        kpis.setCustomerAcquisitionCost(customerAcquisitionCost);
        
        // giá trị trọn đời khách hàng
        // Giả sử có 3 phân khúc khách hàng với giá trị khác nhau
        // Khách hàng A mua:
                // - Năm 1: 5,000,000 VND
                // - Năm 2: 3,000,000 VND  
                // - Năm 3: 2,000,000 VND
                // → CLV = 10,000,000 VND
        BigDecimal avgLifetimeValue = transformedData.getCustomerSegments().getLifetimeValues()
                .values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP); // Average of 3 segments
        kpis.setCustomerLifetimeValue(avgLifetimeValue.doubleValue());
        
        // tỷ lệ rời bỏ (simulation)
        double churnRate = 15.0 + Math.random() * 10; // 15-25%
        kpis.setChurnRate(churnRate);
        
        // tỷ lệ chuyển đổi
        double totalConversionRate = transformedData.getCustomerSegments().getSegments().stream()
                .mapToDouble(TransformedData.CustomerSegments.Segment::getConversionRate)
                .average()
                .orElse(70.0);
        kpis.setConversionRate(totalConversionRate);
        
        // khách hàng mới vs khách hàng lại
        Map<String, Integer> customerCounts = transformedData.getCustomerSegments().getSegments().stream()
                .collect(Collectors.toMap(
                        TransformedData.CustomerSegments.Segment::getSegmentName,
                        TransformedData.CustomerSegments.Segment::getCustomerCount
                ));
        
        kpis.setNewCustomers(customerCounts.getOrDefault("NEW", 0));
        kpis.setReturningCustomers(customerCounts.getOrDefault("RETURNING", 0) + 
                                  customerCounts.getOrDefault("VIP", 0));
        
        // doanh thu hàng tháng (simulation)
        kpis.setMonthlyRecurringRevenue(salesMetrics.getTotalRevenue().multiply(BigDecimal.valueOf(0.3)));
        
        // tỷ lệ hoàn thành đơn hàng
        int totalOrders = salesMetrics.getTotalOrders();
        int fulfilledOrders = totalOrders - salesMetrics.getCancelledOrders();
        double fulfillmentRate = totalOrders > 0 ? (double) fulfilledOrders / totalOrders * 100 : 100.0;
        kpis.setOrderFulfillmentRate(fulfillmentRate);
        
        return kpis;
    }

    /**
     * tạo cảnh báo chất lượng dữ liệu
     */
    private List<TransformedData.AlertDefinition> generateDataQualityAlerts(ExtractedData data) {
        List<TransformedData.AlertDefinition> alerts = new ArrayList<>();
        
        ExtractedData.DataQualityReport qualityReport = data.getQualityReport();
        
        // cảnh báo cấp độ cao dựa trên điểm chất lượng
        if (qualityReport.getQualityScore() < 80.0) {
            TransformedData.AlertDefinition alert = new TransformedData.AlertDefinition();
            alert.setAlertType("ERROR");
            alert.setMessage("Data quality score below threshold: " + qualityReport.getQualityScore() + "%");
            alert.setSeverity("HIGH");
            alert.setContext(Map.of(
                    "qualityScore", qualityReport.getQualityScore(),
                    "totalRecords", qualityReport.getTotalRecords(),
                    "invalidRecords", qualityReport.getInvalidRecords()
            ));
            alert.setTriggeredDate(data.getExtractionDate());
            alerts.add(alert);
        }
        
        // các vấn đề chất lượng cụ thể
        for (String issue : qualityReport.getQualityIssues()) {
            TransformedData.AlertDefinition alert = new TransformedData.AlertDefinition();
            alert.setAlertType("WARNING");
            alert.setMessage(issue);
            alert.setSeverity("MEDIUM");
            alert.setContext(Map.of("issueType", "dataQuality"));
            alert.setTriggeredDate(data.getExtractionDate());
            alerts.add(alert);
        }
        
        // cảnh báo logic kinh doanh
        if (data.getOrders().isEmpty()) {
            TransformedData.AlertDefinition alert = new TransformedData.AlertDefinition();
            alert.setAlertType("WARNING");
            alert.setMessage("No orders found for the specified date");
            alert.setSeverity("MEDIUM");
            alert.setContext(Map.of("date", data.getExtractionDate().toString()));
            alert.setTriggeredDate(data.getExtractionDate());
            alerts.add(alert);
        }
        
        return alerts;
    }

    @Service
    @RequiredArgsConstructor
    @Transactional
    @SuppressWarnings("unchecked")
    public static class SellerDashboardService {    private final EntityManager entityManager;
        private final UserRepository userRepository;
        private final OrderRepository orderRepository;
        private final OrderDetailRepository orderDetailRepository;
        private final RefundRepository refundRepository;
        private final ProductRepository productRepository;
        private final SellerDashboardRepository sellerDashboardRepository;

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
}
