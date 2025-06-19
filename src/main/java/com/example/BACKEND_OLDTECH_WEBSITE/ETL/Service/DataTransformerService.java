package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
}
