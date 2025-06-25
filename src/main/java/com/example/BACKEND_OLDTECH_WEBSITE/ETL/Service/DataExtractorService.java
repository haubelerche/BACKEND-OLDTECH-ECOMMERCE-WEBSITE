package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository.ETLOrderRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository.ETLProductRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service trích xuất dữ liệu - Bước đầu tiên trong quy trình ETL
 * Lấy dữ liệu thô từ các bảng gốc: orders, products, users, sellers
 * Thực hiện basic validation và data quality checks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataExtractorService {

    // Repository để truy vấn dữ liệu từ các bảng khác nhau
    private final ETLOrderRepository etlOrderRepository; // Dữ liệu đơn hàng
    private final UserRepository userRepository; // Dữ liệu khách hàng
    private final ETLProductRepository etlProductRepository; // Dữ liệu sản phẩm
    private final SellerRepository sellerRepository; // Dữ liệu người bán

    /**
     * Phương thức chính: Trích xuất toàn bộ dữ liệu cần thiết cho một ngày
     * Output: ExtractedData chứa tất cả dữ liệu thô cần transform
     */
    public ExtractedData extractDailyData(LocalDate date) {
        log.info("Bắt đầu trích xuất dữ liệu cho ngày: {}", date);
        
        try {
            // Khởi tạo container chứa dữ liệu đã trích xuất
            ExtractedData extractedData = new ExtractedData();
            extractedData.setExtractionDate(date);

            // Trích xuất dữ liệu đơn hàng (orders) cho ngày đó
            extractedData.setOrders(extractOrdersData(date));
            log.info("Đã trích xuất {} đơn hàng", extractedData.getOrders().size());

            // Trích xuất metrics hiệu suất sản phẩm (product performance)
            extractedData.setProductMetrics(extractProductMetrics(date));
            log.info("Đã trích xuất {} metrics sản phẩm", extractedData.getProductMetrics().size());

            // Trích xuất thông tin người bán (seller performance)
            extractedData.setSellerMetrics(extractSellerMetrics(date));
            log.info("Đã trích xuất {} metrics người bán", extractedData.getSellerMetrics().size());

            // Tạo báo cáo chất lượng dữ liệu (data quality assessment)
            extractedData.setQualityReport(generateDataQualityReport(extractedData));

            log.info("Trích xuất dữ liệu hoàn tất cho ngày: {}", date);
            return extractedData;

        } catch (Exception e) {
            log.error("Lỗi trong quá trình trích xuất dữ liệu cho ngày: {}", date, e);
            throw new RuntimeException("Trích xuất dữ liệu thất bại cho ngày: " + date, e);
        }
    }

    /**
     * Trích xuất dữ liệu đơn hàng trong khoảng thời gian 24h của ngày chỉ định
     * Bao gồm: ID đơn hàng, khách hàng, giá trị, trạng thái, phương thức thanh toán
     */
    private List<ExtractedData.OrderData> extractOrdersData(LocalDate date) {
        // Tính toán time range: từ 00:00:00 đến 23:59:59 của ngày đó
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // Query orders trong time range và mapping sang OrderData object
        return etlOrderRepository.findOrdersByDateRange(Timestamp.valueOf(startOfDay), Timestamp.valueOf(endOfDay))
                .stream()
                .map(order -> {
                    ExtractedData.OrderData orderData = new ExtractedData.OrderData();
                    orderData.setOrderId(order.getOrderId());
                    orderData.setUserId(order.getUserId());
                    orderData.setSellerId(null); // Orders table không có direct sellerId reference
                    orderData.setTotalAmount(order.getTotalAmount());
                    orderData.setStatus(order.getStatus() != null ? order.getStatus().name() : "UNKNOWN");
                    orderData.setOrderDate(order.getCreatedAt().toLocalDateTime().toLocalDate());
                    orderData.setProductCategory("Phụ kiện công nghệ"); // Sẽ được phân loại chi tiết trong transform phase
                    orderData.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "UNKNOWN");
                    orderData.setDeliveryAddress(order.getShippingAddress() != null ? 
                        order.getShippingAddress().getStreet() : "N/A");
                    
                    return orderData;
                })
                .collect(Collectors.toList());
    }

    /**
     * Trích xuất performance metrics của từng sản phẩm
     * Tính toán: số lượt mua, doanh thu, views (nếu có)
     */
    private List<ExtractedData.ProductMetrics> extractProductMetrics(LocalDate date) {
        return etlProductRepository.findAll()
                .stream()
                .map(product -> {
                    ExtractedData.ProductMetrics metrics = new ExtractedData.ProductMetrics();
                    metrics.setProductId(product.getProductId());
                    metrics.setProductName(product.getName());
                    metrics.setCategory("Phụ kiện công nghệ"); // Sẽ được phân loại chi tiết dựa trên tên sản phẩm
                    metrics.setStockLevel(0); // Product table thiếu stock quantity field
                    
                    // Tính performance metrics dựa trên sales data
                    LocalDateTime startOfDay = date.atStartOfDay();
                    LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
                    
                    // Query aggregated stats cho product này trong ngày
                    List<Object[]> productStats = etlOrderRepository.getProductStatsByDateRange(
                            product.getProductId(), Timestamp.valueOf(startOfDay), Timestamp.valueOf(endOfDay));
                    
                    if (!productStats.isEmpty()) {
                        Object[] stats = productStats.get(0);
                        metrics.setPurchaseCount(((Number) stats[0]).intValue()); // Số lượt mua
                        metrics.setRevenue((BigDecimal) stats[1]); // Tổng doanh thu
                    } else {
                        metrics.setPurchaseCount(0);
                        metrics.setRevenue(BigDecimal.ZERO);
                    }
                    
                    // Đặt giá trị mặc định cho các thông số không có dữ liệu trực tiếp
                    metrics.setViewCount(metrics.getPurchaseCount() * 10); // Giả lập số lượt xem
                    metrics.setAverageRating(4.0 + Math.random()); // Giả lập đánh giá trung bình
                    
                    return metrics;
                })
                .collect(Collectors.toList());
    }

    /**
     * Trích xuất thông tin người bán (seller performance)
     * Hiện tại chỉ trả về danh sách rỗng, cần bổ sung logic thực tế nếu muốn trích xuất dữ liệu thật
     */
    private List<ExtractedData.SellerMetrics> extractSellerMetrics(LocalDate date) {
        // TODO: Implement actual seller metrics extraction logic
        // Dưới đây là ví dụ trả về danh sách rỗng, bạn cần thay bằng logic thực tế
        return new ArrayList<>();
    }

    /**
     * Tạo báo cáo chất lượng dữ liệu
     */
    private ExtractedData.DataQualityReport generateDataQualityReport(ExtractedData data) {
        ExtractedData.DataQualityReport report = new ExtractedData.DataQualityReport();
        
        int totalRecords = data.getOrders().size() + data.getProductMetrics().size() + data.getSellerMetrics().size();
        
        List<String> qualityIssues = new ArrayList<>();
        int invalidRecords = 0;
        
        // Kiểm tra chất lượng dữ liệu đơn hàng
        for (ExtractedData.OrderData order : data.getOrders()) {
            if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                qualityIssues.add("Order " + order.getOrderId() + " has invalid amount");
                invalidRecords++;
            }
            if (order.getStatus() == null || order.getStatus().equals("UNKNOWN")) {
                qualityIssues.add("Order " + order.getOrderId() + " has missing status");
                invalidRecords++;
            }
        }
        
        int validRecords = totalRecords - invalidRecords;
        double qualityScore = totalRecords > 0 ? (double) validRecords / totalRecords * 100 : 100.0;
        
        report.setTotalRecords(totalRecords);
        report.setValidRecords(validRecords);
        report.setInvalidRecords(invalidRecords);
        report.setQualityScore(qualityScore);
        report.setQualityIssues(qualityIssues);
        
        return report;
    }

    /**
     * Trích xuất toàn bộ dữ liệu cần thiết cho một tuần (từ thứ 2 đến chủ nhật)
     * Output: ExtractedData chứa tất cả dữ liệu thô cần transform cho cả tuần
     */
    public ExtractedData extractWeeklyData(LocalDate weekStartDate) {
        log.info("Bắt đầu trích xuất dữ liệu cho tuần bắt đầu: {}", weekStartDate);
        try {
            ExtractedData extractedData = new ExtractedData();
            extractedData.setExtractionDate(weekStartDate);

            // Gom dữ liệu từng ngày trong tuần
            List<ExtractedData.OrderData> allOrders = new ArrayList<>();
            List<ExtractedData.ProductMetrics> allProductMetrics = new ArrayList<>();
            List<ExtractedData.SellerMetrics> allSellerMetrics = new ArrayList<>();

            for (int i = 0; i < 7; i++) {
                LocalDate date = weekStartDate.plusDays(i);
                allOrders.addAll(extractOrdersData(date));
                allProductMetrics.addAll(extractProductMetrics(date));
                allSellerMetrics.addAll(extractSellerMetrics(date));
            }

            extractedData.setOrders(allOrders);
            extractedData.setProductMetrics(allProductMetrics);
            extractedData.setSellerMetrics(allSellerMetrics);

            // Tạo báo cáo chất lượng dữ liệu cho cả tuần
            extractedData.setQualityReport(generateDataQualityReport(extractedData));

            log.info("Trích xuất dữ liệu hoàn tất cho tuần bắt đầu: {}", weekStartDate);
            return extractedData;
        } catch (Exception e) {
            log.error("Lỗi trong quá trình trích xuất dữ liệu cho tuần: {}", weekStartDate, e);
            throw new RuntimeException("Trích xuất dữ liệu thất bại cho tuần: " + weekStartDate, e);
        }
    }
}
