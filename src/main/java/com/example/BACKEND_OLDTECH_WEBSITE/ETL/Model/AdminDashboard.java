package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Bảng lưu trữ dữ liệu dashboard của admin
 * Tổng hợp hiệu suất toàn bộ nền tảng theo từng ngày/tuần/tháng
 */
@Entity
@Table(name = "admin_dashboard")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dashboard_id", columnDefinition = "INT UNSIGNED")
    private Integer dashboardId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "period_type", length = 20, nullable = false)
    private String periodType; // DAILY, WEEKLY, MONTHLY, QUARTERLY

    // ===============================
    // HIỆU SUẤT BÁN HÀNG
    // ===============================
    
    @Column(name = "gross_merchandise_value", precision = 18, scale = 2)
    private BigDecimal grossMerchandiseValue; // Tổng giá trị giao dịch (GMV)

    @Column(name = "average_order_value", precision = 15, scale = 2)
    private BigDecimal averageOrderValue; // Giá trị đơn hàng trung bình (AOV)

    @Column(name = "platform_revenue", precision = 18, scale = 2)
    private BigDecimal platformRevenue; // Doanh thu nền tảng (hoa hồng)

    @Column(name = "total_orders", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer totalOrders; // Tổng số đơn hàng

    @Column(name = "total_return_orders", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer totalReturnOrders; // Tổng số đơn đổi trả

    @Column(name = "return_orders_value", precision = 15, scale = 2)
    private BigDecimal returnOrdersValue; // Giá trị đơn đổi trả

    @Column(name = "net_gmv", precision = 18, scale = 2)
    private BigDecimal netGmv; // GMV thực (sau khi trừ đổi trả)

    // ===============================
    // HIỆU SUẤT NGƯỜI DÙNG
    // ===============================
    
    @Column(name = "website_visits", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer websiteVisits; // Lượt truy cập website

    @Column(name = "unique_visitors", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer uniqueVisitors; // Khách truy cập duy nhất

    @Column(name = "new_users", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer newUsers; // Người dùng mới đăng ký

    @Column(name = "new_buyers", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer newBuyers; // Người mua mới

    @Column(name = "new_sellers", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer newSellers; // Người bán mới

    @Column(name = "conversion_rate", precision = 5, scale = 4)
    private BigDecimal conversionRate; // Tỷ lệ chuyển đổi (%)

    @Column(name = "returning_customer_orders", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer returningCustomerOrders; // Đơn hàng từ khách cũ

    @Column(name = "customer_return_rate", precision = 5, scale = 4)
    private BigDecimal customerReturnRate; // Tỷ lệ khách hàng quay lại (%)

    @Column(name = "order_return_rate", precision = 5, scale = 4)
    private BigDecimal orderReturnRate; // Tỷ lệ đổi trả đơn hàng (%)

    // ===============================
    // CẢNH BÁO & HOẠT ĐỘNG
    // ===============================
    
    @Column(name = "pending_sellers", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer pendingSellers; // Người bán chờ duyệt

    @Column(name = "new_complaints", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer newComplaints; // Khiếu nại mới

    @Column(name = "fraud_transactions", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer fraudTransactions; // Giao dịch nghi vấn gian lận

    @Column(name = "system_alerts", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer systemAlerts; // Cảnh báo hệ thống

    @Column(name = "pending_products", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer pendingProducts; // Sản phẩm chờ duyệt

    @Column(name = "reported_products", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer reportedProducts; // Sản phẩm bị báo cáo

    // ===============================
    // THÔNG TIN BỔ SUNG
    // ===============================
    
    @Column(name = "active_sellers", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer activeSellers; // Người bán đang hoạt động

    @Column(name = "active_buyers", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer activeBuyers; // Người mua đang hoạt động

    @Column(name = "total_products", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer totalProducts; // Tổng số sản phẩm

    @Column(name = "active_products", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer activeProducts; // Sản phẩm đang hoạt động

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate; // Tỷ lệ hoa hồng trung bình

    @Column(name = "avg_processing_time", precision = 8, scale = 2)
    private BigDecimal avgProcessingTime; // Thời gian xử lý đơn hàng trung bình (giờ)

    @Column(name = "avg_delivery_time", precision = 8, scale = 2)
    private BigDecimal avgDeliveryTime; // Thời gian giao hàng trung bình (ngày)

    // ===============================
    // METADATA
    // ===============================
    
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "data_quality_score", precision = 3, scale = 2)
    private BigDecimal dataQualityScore; // Điểm chất lượng dữ liệu (0-1)

    @Column(name = "is_forecast", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isForecast; // Dữ liệu dự đoán hay thực tế

    @Column(name = "calculation_method", length = 50)
    private String calculationMethod; // Phương pháp tính toán (MANUAL, AUTO, ARIMA)

    // ===============================
    // HELPER METHODS
    // ===============================
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        if (isForecast == null) isForecast = false;
        if (dataQualityScore == null) dataQualityScore = BigDecimal.ONE;
        if (calculationMethod == null) calculationMethod = "AUTO";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Tính tỷ lệ chuyển đổi từ lượt truy cập thành đơn hàng
     */
    public void calculateConversionRate() {
        if (websiteVisits != null && websiteVisits > 0 && totalOrders != null) {
            this.conversionRate = BigDecimal.valueOf(totalOrders)
                    .divide(BigDecimal.valueOf(websiteVisits), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.conversionRate = BigDecimal.ZERO;
        }
    }

    /**
     * Tính tỷ lệ khách hàng quay lại
     */
    public void calculateCustomerReturnRate() {
        if (totalOrders != null && totalOrders > 0 && returningCustomerOrders != null) {
            this.customerReturnRate = BigDecimal.valueOf(returningCustomerOrders)
                    .divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.customerReturnRate = BigDecimal.ZERO;
        }
    }

    /**
     * Tính tỷ lệ đổi trả đơn hàng
     */
    public void calculateOrderReturnRate() {
        if (totalOrders != null && totalOrders > 0 && totalReturnOrders != null) {
            this.orderReturnRate = BigDecimal.valueOf(totalReturnOrders)
                    .divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.orderReturnRate = BigDecimal.ZERO;
        }
    }

    /**
     * Tính giá trị đơn hàng trung bình
     */
    public void calculateAverageOrderValue() {
        if (totalOrders != null && totalOrders > 0 && grossMerchandiseValue != null) {
            this.averageOrderValue = grossMerchandiseValue
                    .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        } else {
            this.averageOrderValue = BigDecimal.ZERO;
        }
    }

    /**
     * Tính GMV thực
     */
    public void calculateNetGmv() {
        if (grossMerchandiseValue != null) {
            BigDecimal returnValue = returnOrdersValue != null ? returnOrdersValue : BigDecimal.ZERO;
            this.netGmv = grossMerchandiseValue.subtract(returnValue);
        } else {
            this.netGmv = BigDecimal.ZERO;
        }
    }

    /**
     * Tính doanh thu nền tảng
     */
    public void calculatePlatformRevenue() {
        if (grossMerchandiseValue != null && commissionRate != null) {
            this.platformRevenue = grossMerchandiseValue
                    .multiply(commissionRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (grossMerchandiseValue != null) {
            // Mặc định 5% hoa hồng
            this.platformRevenue = grossMerchandiseValue
                    .multiply(BigDecimal.valueOf(0.05));
        } else {
            this.platformRevenue = BigDecimal.ZERO;
        }
    }

    /**
     * Validate dữ liệu
     */
    public boolean isDataValid() {
        return grossMerchandiseValue != null && 
               grossMerchandiseValue.compareTo(BigDecimal.ZERO) >= 0 &&
               totalOrders != null && totalOrders >= 0 &&
               websiteVisits != null && websiteVisits >= 0;
    }

    /**
     * Tính tổng cảnh báo
     */
    public Integer getTotalAlerts() {
        int total = 0;
        if (pendingSellers != null) total += pendingSellers;
        if (newComplaints != null) total += newComplaints;
        if (fraudTransactions != null) total += fraudTransactions;
        if (systemAlerts != null) total += systemAlerts;
        if (pendingProducts != null) total += pendingProducts;
        if (reportedProducts != null) total += reportedProducts;
        return total;
    }
}
