package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Seller;

/**
 * Bảng lưu trữ dữ liệu dashboard của seller
 * Tổng hợp hiệu suất kinh doanh theo từng ngày/tuần/tháng
 */
@Entity
@Table(name = "seller_dashboard")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerDashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dashboard_id", columnDefinition = "INT UNSIGNED")
    private Integer dashboardId;

    @Column(name = "seller_id", columnDefinition = "INT UNSIGNED", nullable = false)
    private Integer sellerId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "period_type", length = 20, nullable = false)
    private String periodType; // DAILY, WEEKLY, MONTHLY, QUARTERLY

    // ===============================
    // HIỆU SUẤT BÁN HÀNG
    // ===============================
    
    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue; // Doanh thu

    @Column(name = "total_orders", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer totalOrders; // Số đơn hàng

    @Column(name = "average_order_value", precision = 15, scale = 2)
    private BigDecimal averageOrderValue; // Giá trị đơn hàng trung bình

    @Column(name = "return_orders_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer returnOrdersCount; // Số đơn đổi trả

    @Column(name = "return_orders_value", precision = 15, scale = 2)
    private BigDecimal returnOrdersValue; // Giá trị đơn đổi trả

    @Column(name = "net_revenue", precision = 15, scale = 2)
    private BigDecimal netRevenue; // Doanh thu thực (sau khi trừ đổi trả)

    // ===============================
    // HIỆU SUẤT NGƯỜI DÙNG
    // ===============================
    
    @Column(name = "total_visits", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer totalVisits; // Lượt truy cập gian hàng

    @Column(name = "unique_visitors", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer uniqueVisitors; // Khách truy cập duy nhất

    @Column(name = "conversion_rate", precision = 5, scale = 4)
    private BigDecimal conversionRate; // Tỷ lệ chuyển đổi (%)

    @Column(name = "returning_customer_orders", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer returningCustomerOrders; // Đơn hàng từ khách cũ

    @Column(name = "new_customer_orders", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer newCustomerOrders; // Đơn hàng từ khách mới

    @Column(name = "customer_return_rate", precision = 5, scale = 4)
    private BigDecimal customerReturnRate; // Tỷ lệ khách hàng quay lại (%)

    @Column(name = "return_rate", precision = 5, scale = 4)
    private BigDecimal returnRate; // Tỷ lệ đổi trả (%)

    // ===============================
    // THÔNG TIN BỔ SUNG
    // ===============================
    
    @Column(name = "successful_orders", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer successfulOrders; // Đơn hàng thành công

    @Column(name = "cancelled_orders", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer cancelledOrders; // Đơn hàng bị hủy

    @Column(name = "pending_orders", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer pendingOrders; // Đơn hàng chờ xử lý

    @Column(name = "shipped_orders", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer shippedOrders; // Đơn hàng đã giao

    @Column(name = "products_sold", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer productsSold; // Số sản phẩm đã bán

    @Column(name = "unique_products_sold", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer uniqueProductsSold; // Số loại sản phẩm khác nhau đã bán

    @Column(name = "avg_delivery_time", precision = 5, scale = 2)
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

    // ===============================
    // RELATIONSHIPS
    // ===============================
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private Seller seller;

    // ===============================
    // HELPER METHODS
    // ===============================
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        if (isForecast == null) isForecast = false;
        if (dataQualityScore == null) dataQualityScore = BigDecimal.ONE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }    /**
     * Tính tỷ lệ chuyển đổi từ lượt truy cập thành đơn hàng
     */
    public void calculateConversionRate() {
        if (totalVisits != null && totalVisits > 0 && totalOrders != null) {
            this.conversionRate = BigDecimal.valueOf(totalOrders)
                    .divide(BigDecimal.valueOf(totalVisits), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.conversionRate = BigDecimal.ZERO;
        }
    }    /**
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
     * Tính tỷ lệ đổi trả
     */
    public void calculateReturnRate() {
        if (totalOrders != null && totalOrders > 0 && returnOrdersCount != null) {
            this.returnRate = BigDecimal.valueOf(returnOrdersCount)
                    .divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.returnRate = BigDecimal.ZERO;
        }
    }

    /**
     * Tính giá trị đơn hàng trung bình
     */
    public void calculateAverageOrderValue() {
        if (totalOrders != null && totalOrders > 0 && totalRevenue != null) {
            this.averageOrderValue = totalRevenue
                    .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        } else {
            this.averageOrderValue = BigDecimal.ZERO;
        }
    }

    /**
     * Tính doanh thu thực
     */
    public void calculateNetRevenue() {
        if (totalRevenue != null) {
            BigDecimal returnValue = returnOrdersValue != null ? returnOrdersValue : BigDecimal.ZERO;
            this.netRevenue = totalRevenue.subtract(returnValue);
        } else {
            this.netRevenue = BigDecimal.ZERO;
        }
    }
}
