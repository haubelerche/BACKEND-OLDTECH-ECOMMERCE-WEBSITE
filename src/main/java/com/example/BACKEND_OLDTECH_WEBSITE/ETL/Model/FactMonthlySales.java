package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bảng phân tích doanh số hàng tháng
 * Lưu trữ dữ liệu doanh số hàng tháng cho phân tích sản phẩm công nghệ
 * Tối ưu hóa cho: Laptop, Điện thoại, PC, Tai nghe, Loa, Camera, Tablet, Smart Watch, Phụ kiện công nghệ
 * Lưu ý: Mỗi sản phẩm có số lượng = 1, không có vận chuyển, chỉ có thị trường trung gian
 */
@Entity
@Table(name = "fact_monthly_sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactMonthlySales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "month_key", nullable = false)
    private LocalDate monthKey; // Ngày đầu tháng
    @Column(name = "`year_month`", nullable = false, length = 7)
    private String yearMonth; // Định dạng: "2025-06"

    // Các chỉ số doanh số hàng tháng
    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue; // Giá trị giao dịch

    @Column(name = "total_orders")
    private Integer totalOrders; // Tổng số đơn hàng

    @Column(name = "unique_buyers")
    private Integer uniqueBuyers; // Số lượng khách hàng

    @Column(name = "unique_sellers")
    private Integer uniqueSellers; // Số lượng người bán    @Column(name = "average_order_value", precision = 10, scale = 2)
    private BigDecimal averageOrderValue; // Giá trị trung bình của đơn hàng

    @Column(name = "cancelled_orders")
    private Integer cancelledOrders; // Số lượng đơn hàng bị hủy    @Column(name = "refund_amount", precision = 15, scale = 2)
    private BigDecimal refundAmount; // Số tiền hoàn trả

    // Các chỉ số của thị trường công nghệ (mỗi sản phẩm có số lượng = 1, nhưng đơn hàng có thể có nhiều sản phẩm)
    @Column(name = "tech_items_sold")
    private Integer techItemsSold; // Tổng số sản phẩm công nghệ đã bán (tổng số sản phẩm trong tất cả đơn hàng)

    @Column(name = "tech_items_listed")
    private Integer techItemsListed; // Tổng số sản phẩm công nghệ đã đăng bán

    @Column(name = "listing_to_sale_rate")
    private Double listingToSaleRate; // Phần trăm sản phẩm đã đăng bán

    @Column(name = "avg_listing_duration_days")
    private Double avgListingDurationDays; // Số ngày từ khi đăng bán đến khi bán

    @Column(name = "new_sellers_count")
    private Integer newSellersCount; // Số lượng người bán mới trong tháng

    @Column(name = "repeat_buyers_count")
    private Integer repeatBuyersCount; // Số lượng khách hàng lại trong tháng

    // Phân tích theo ngành hàng (các ngành hàng chính)
    @Column(name = "laptops_sold")
    private Integer laptopsSold;

    @Column(name = "phones_sold")
    private Integer phonesSold;

    @Column(name = "pcs_sold")
    private Integer pcsSold; // PC Desktop

    @Column(name = "headphones_sold")
    private Integer headphonesSold;

    @Column(name = "speakers_sold")
    private Integer speakersSold;

    @Column(name = "cameras_sold")
    private Integer camerasSold;

    @Column(name = "tablets_sold")
    private Integer tabletsSold;    @Column(name = "smartwatches_sold")
    private Integer smartwatchesSold;

    @Column(name = "accessories_sold")
    private Integer accessoriesSold; // E-Accessories

    // Sức khỏe của thị trường công nghệ (không có chỉ số vận chuyển)
    @Column(name = "seller_satisfaction_rate", precision = 5, scale = 4)
    private BigDecimal sellerSatisfactionRate;

    @Column(name = "buyer_satisfaction_rate", precision = 5, scale = 4)
    private BigDecimal buyerSatisfactionRate;

    @Column(name = "avg_tech_item_age_years")
    private Double avgTechItemAgeYears; // Tuổi trung bình của sản phẩm công nghệ đã bán

    @Column(name = "premium_items_percentage")
    private Double premiumItemsPercentage; // Phần trăm sản phẩm có giá trị cao

    @Column(name = "data_quality_score")
    private Double dataQualityScore;

    @Column(name = "etl_processed_at")
    private LocalDateTime etlProcessedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {        updatedAt = LocalDateTime.now();
    }

    /**
     * Tính toán tốc độ của thị trường công nghệ (số lượng sản phẩm công nghệ đã bán trung bình mỗi ngày)
     */
    public Double getTechMarketplaceVelocity() {
        if (techItemsSold == null || techItemsSold == 0) return 0.0;
        // Assume 30 days per month average
        return techItemsSold / 30.0;
            }    /**
     * Tính toán sản lượng của người bán (số lượng sản phẩm công nghệ đã bán trung bình mỗi người bán)
     */
    public Double getSellerProductivity() {
        if (uniqueSellers == null || uniqueSellers == 0) return 0.0;
        if (techItemsSold == null) return 0.0;
        return techItemsSold.doubleValue() / uniqueSellers.doubleValue();
    }

    /**
     * Kiểm tra xem tháng này có hoạt động thị trường công nghệ khỏe mạnh không
     */
    public boolean isHealthyTechMarketplace() {
        return listingToSaleRate != null && listingToSaleRate > 0.15 && // 15% tỷ lệ đăng bán đến bán
               uniqueSellers != null && uniqueSellers > 0 &&
               totalOrders != null && totalOrders > 0;
    }

    /**
     * Tính toán giá trị trung bình của đơn hàng 
     */
    public Double getAverageTechOrderValue() {
        if (totalOrders == null || totalOrders == 0) return 0.0;
        if (totalRevenue == null) return 0.0;
        return totalRevenue.doubleValue() / totalOrders.doubleValue();
    }

    /**
     * Lấy ngành hàng công nghệ phổ biến nhất trong tháng này
     */
    public String getMostPopularTechCategory() {
        int maxSold = 0;
        String category = "NONE";
        
        if (laptopsSold != null && laptopsSold > maxSold) { maxSold = laptopsSold; category = "LAPTOPS"; }
        if (phonesSold != null && phonesSold > maxSold) { maxSold = phonesSold; category = "PHONES"; }
        if (pcsSold != null && pcsSold > maxSold) { maxSold = pcsSold; category = "PCS"; }
        if (headphonesSold != null && headphonesSold > maxSold) { maxSold = headphonesSold; category = "HEADPHONES"; }
        if (speakersSold != null && speakersSold > maxSold) { maxSold = speakersSold; category = "SPEAKERS"; }
        if (camerasSold != null && camerasSold > maxSold) { maxSold = camerasSold; category = "CAMERAS"; }
        if (tabletsSold != null && tabletsSold > maxSold) { maxSold = tabletsSold; category = "TABLETS"; }
        if (smartwatchesSold != null && smartwatchesSold > maxSold) { maxSold = smartwatchesSold; category = "SMARTWATCHES"; }
        if (accessoriesSold != null && accessoriesSold > maxSold) { category = "ACCESSORIES"; }
        
        return category;
    }
}
