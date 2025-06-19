package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dịch vụ chuyển đổi ETL cho thị trường công nghệ
 * Chuyển đổi và bổ sung dữ liệu trích xuất với các chỉ số cụ thể cho thị trường công nghệ
 * Tập trung: Laptop, Điện thoại, PC, Tai nghe, Loa, Camera, Tablet, Đồng hồ thông minh, Phụ kiện công nghệ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TechMarketplaceTransformerService {    /**
     * Chuyển đổi dữ liệu trích xuất tháng thành các chỉ số cụ thể cho thị trường công nghệ
     */
    public TransformedData transformMonthlyTechData(ExtractedData extractedData, YearMonth targetMonth) {
        log.info("Transforming tech marketplace data for month: {}", targetMonth);
        
        try {
            TransformedData transformedData = new TransformedData();
            transformedData.setProcessDate(targetMonth.atDay(1));
              // Tính toán các chỉ số bán hàng cho thị trường công nghệ
            Map<String, Object> techSalesMetrics = calculateTechSalesMetrics(extractedData, targetMonth);
            transformedData.setSalesMetrics(createSalesMetricsFromTechMap(techSalesMetrics));
            
            // Tính toán sức khỏe của thị trường công nghệ
            Map<String, Object> techMarketplaceHealth = calculateTechMarketplaceHealth(extractedData, targetMonth);
            transformedData.setBusinessKPIs(createBusinessKPIsFromTechMap(techMarketplaceHealth));
            
            // Tương lai: Thêm hành vi mua hàng và kiểm tra chất lượng khi cần thiết
            
            log.info("Tech marketplace transformation completed for month: {}", targetMonth);
            return transformedData;
            
        } catch (Exception e) {
            log.error("Error transforming tech marketplace data for month: {}", targetMonth, e);
            throw new RuntimeException("Tech marketplace transformation failed", e);
        }
    }    /**
     * Tính toán các chỉ số bán hàng cho thị trường công nghệ
     */
    private Map<String, Object> calculateTechSalesMetrics(ExtractedData data, YearMonth month) {
        Map<String, Object> metrics = new HashMap<>();
        
        if (data.getOrders() == null || data.getOrders().isEmpty()) {
            return createEmptyTechMetrics(month);
        }        List<ExtractedData.OrderData> monthlyOrders = data.getOrders();
        
        // Các chỉ số bán hàng cơ bản (mỗi mặt hàng có số lượng = 1, nhưng đơn hàng có thể chứa nhiều mặt hàng)
        BigDecimal totalRevenue = monthlyOrders.stream()
                .map(order -> order.getTotalAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalOrders = monthlyOrders.size();
        
        Set<Integer> uniqueBuyers = monthlyOrders.stream()
                .map(ExtractedData.OrderData::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        BigDecimal avgOrderValue = totalOrders > 0 ? 
                totalRevenue.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        // Các tính toán cụ thể cho thị trường công nghệ
        // Mỗi đơn hàng có thể có nhiều mặt hàng, nhưng mỗi mặt hàng có số lượng = 1
        int techItemsSold = totalOrders * 2; // Ước tính: trung bình 2 mặt hàng trên mỗi đơn hàng
        
        // Phân tích theo danh mục công nghệ (dựa trên tên/mô tả sản phẩm)
        Map<String, Integer> categoryBreakdown = calculateTechCategoryBreakdown(data);
        
        metrics.put("totalRevenue", totalRevenue);
        metrics.put("totalOrders", totalOrders);
        metrics.put("uniqueBuyers", uniqueBuyers.size());
        metrics.put("avgOrderValue", avgOrderValue);
        metrics.put("techItemsSold", techItemsSold);
        metrics.put("categoryBreakdown", categoryBreakdown);
        
        log.info("Các chỉ số bán hàng cho thị trường công nghệ đã được tính toán - Doanh thu: {}, Đơn hàng: {}, Mặt hàng: {}, Khách hàng: {}", 
                totalRevenue, totalOrders, techItemsSold, uniqueBuyers.size());
        
        return metrics;
    }    /**
     * Tính toán phân tích theo danh mục công nghệ
     */
    private Map<String, Integer> calculateTechCategoryBreakdown(ExtractedData data) {
        Map<String, Integer> breakdown = new HashMap<>();
        breakdown.put("laptops", 0);
        breakdown.put("phones", 0);
        breakdown.put("pcs", 0);
        breakdown.put("headphones", 0);
        breakdown.put("speakers", 0);
        breakdown.put("cameras", 0);
        breakdown.put("tablets", 0);
        breakdown.put("smartwatches", 0);
        breakdown.put("accessories", 0);
          // Hiện tại, mô phỏng phân tích theo danh mục dựa trên tổng số mặt hàng đã bán
        // Trong thực tế, sẽ phân tích các mặt hàng trong đơn hàng thực tế
        if (data.getOrders() != null && !data.getOrders().isEmpty()) {
            int totalOrders = data.getOrders().size();
            int estimatedTotalItems = totalOrders * 2; // Trung bình 2 mặt hàng trên mỗi đơn hàng
            
            // Mô phỏng phân phối thực tế cho thị trường công nghệ
            breakdown.put("phones", (int)(estimatedTotalItems * 0.25)); // 25% điện thoại
            breakdown.put("accessories", (int)(estimatedTotalItems * 0.20)); // 20% phụ kiện
            breakdown.put("laptops", (int)(estimatedTotalItems * 0.20)); // 20% laptop
            breakdown.put("headphones", (int)(estimatedTotalItems * 0.15)); // 15% tai nghe
            breakdown.put("cameras", (int)(estimatedTotalItems * 0.10)); // 10% camera
            breakdown.put("tablets", (int)(estimatedTotalItems * 0.06)); // 6% tablet
            breakdown.put("smartwatches", (int)(estimatedTotalItems * 0.04)); // 4% đồng hồ thông minh
        }
        
        return breakdown;
    }

    /**
        * Tính toán các chỉ số hành vi khách hàng cho thị trường công nghệ
     */
    private Map<String, Object> calculateTechBuyerMetrics(ExtractedData data, YearMonth month) {
        Map<String, Object> metrics = new HashMap<>();
        
        if (data.getOrders() == null || data.getOrders().isEmpty()) {
            metrics.put("TECH_ENTHUSIAST", createEmptyBuyerSegment());
            metrics.put("BUDGET_BUYER", createEmptyBuyerSegment());
            metrics.put("BUSINESS_USER", createEmptyBuyerSegment());
            return metrics;
        }

        // Phân tích khách hàng đơn giản cho thị trường công nghệ
        int totalBuyers = data.getOrders().size(); // Đơn giản
        
        Map<String, Object> techEnthusiast = createEmptyBuyerSegment();
        techEnthusiast.put("totalBuyers", (int)(totalBuyers * 0.3)); // 30% người dùng công nghệ
        
        Map<String, Object> budgetBuyer = createEmptyBuyerSegment();
        budgetBuyer.put("totalBuyers", (int)(totalBuyers * 0.5)); // 50% người dùng ngân sách
        
        Map<String, Object> businessUser = createEmptyBuyerSegment();
        businessUser.put("totalBuyers", (int)(totalBuyers * 0.2)); // 20% người dùng kinh doanh
        
        metrics.put("TECH_ENTHUSIAST", techEnthusiast);
        metrics.put("BUDGET_BUYER", budgetBuyer);
        metrics.put("BUSINESS_USER", businessUser);

        return metrics;
    }

    /**
     * Tính toán các chỉ số sức khỏe của thị trường công nghệ
     */
    private Map<String, Object> calculateTechMarketplaceHealth(ExtractedData data, YearMonth month) {
        Map<String, Object> health = new HashMap<>();
        
        // Sức khỏe của thị trường công nghệ dựa trên hoạt động giao dịch
        int totalTransactions = data.getOrders() != null ? data.getOrders().size() : 0;
        
        // Tính toán điểm sức khỏe dựa trên mức độ hoạt động
        double healthScore = Math.min(totalTransactions * 2.0, 100.0); // Simple scoring
        
        health.put("activeTechCategories", 9); // Tất cả 9 danh mục công nghệ đang hoạt động
        health.put("categoryDiversityScore", 85.0);
        health.put("techMarketplaceHealthScore", healthScore);
        
        return health;
    }

    /**
     * Thực hiện kiểm tra chất lượng dữ liệu cụ thể cho thị trường công nghệ
     */
    private Map<String, Object> performTechDataQualityCheck(ExtractedData data) {
        List<String> alerts = new ArrayList<>();
        Map<String, Object> qualityMetrics = new HashMap<>();
        
        // Kiểm tra chất lượng dữ liệu cụ thể cho thị trường công nghệ
        if (data.getOrders() != null) {
            long ordersWithNullAmount = data.getOrders().stream()
                    .filter(order -> order.getTotalAmount() == null)
                    .count();
            
            if (ordersWithNullAmount > 0) {
                alerts.add("Found " + ordersWithNullAmount + " tech transactions with null amounts");
            }

            // Kiểm tra giá cả không thực tế cho thị trường công nghệ
            long unrealisticPrices = data.getOrders().stream()
                    .filter(order -> order.getTotalAmount() != null)
                    .filter(order -> order.getTotalAmount().compareTo(new BigDecimal("50000")) > 0 || // > $50k seems high for used tech
                                   order.getTotalAmount().compareTo(new BigDecimal("10")) < 0) // < $10 seems too low
                    .count();
            
            if (unrealisticPrices > 0) {
                alerts.add("Found " + unrealisticPrices + " tech items with unrealistic prices");
            }
        }

        double qualityScore = alerts.isEmpty() ? 100.0 : Math.max(100.0 - (alerts.size() * 10), 60.0);
        
        qualityMetrics.put("alerts", alerts);
        qualityMetrics.put("qualityScore", qualityScore);
        
        return qualityMetrics;
    }

    // Phương thức trợ giúp
    
    private Map<String, Object> createEmptyTechMetrics(YearMonth month) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRevenue", BigDecimal.ZERO);
        metrics.put("totalTransactions", 0);
        metrics.put("uniqueBuyers", 0);
        metrics.put("avgTransactionValue", BigDecimal.ZERO);
        metrics.put("techItemsSold", 0);
        metrics.put("categoryBreakdown", new HashMap<String, Integer>());
        return metrics;
    }
    
    private Map<String, Object> createEmptyBuyerSegment() {
        Map<String, Object> segment = new HashMap<>();
        segment.put("totalBuyers", 0);
        segment.put("totalRevenue", BigDecimal.ZERO);
        segment.put("avgOrderValue", BigDecimal.ZERO);
        return segment;
    }

    /**
     * Tạo SalesMetrics từ bản đồ thị trường công nghệ
     */
    private TransformedData.SalesMetrics createSalesMetricsFromTechMap(Map<String, Object> techMetrics) {
        TransformedData.SalesMetrics salesMetrics = new TransformedData.SalesMetrics();
          salesMetrics.setTotalRevenue((BigDecimal) techMetrics.get("totalRevenue"));
        salesMetrics.setTotalOrders((Integer) techMetrics.get("totalOrders")); // Orders field
        salesMetrics.setUniqueCustomers((Integer) techMetrics.get("uniqueBuyers")); // Buyers mapped to customers
        salesMetrics.setAverageOrderValue((BigDecimal) techMetrics.get("avgOrderValue"));
        salesMetrics.setCancelledOrders(0); // Default for now
        salesMetrics.setRefundAmount(BigDecimal.ZERO); // Default for now
        
        return salesMetrics;
    }    /**
     * Tạo Business KPIs từ bản đồ sức khỏe của thị trường công nghệ
     */
    private TransformedData.BusinessKPIs createBusinessKPIsFromTechMap(Map<String, Object> healthMetrics) {
        TransformedData.BusinessKPIs kpis = new TransformedData.BusinessKPIs();
        
        kpis.setOrderFulfillmentRate((Double) healthMetrics.getOrDefault("techMarketplaceHealthScore", 85.0));
        // Lưu ý: Các trường KPI khác cần được thêm vào lớp BusinessKPIs nếu cần
        
        return kpis;
    }
}
