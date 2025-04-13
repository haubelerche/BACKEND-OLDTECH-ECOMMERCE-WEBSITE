package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics;

import lombok.Data;

@Data
public class WebsiteStatisticsDTO {
    private Long websiteVisits;
    private Double CAC; // chi phí thu hút khách hàng)
    private Double conversionRate; // Tỷ lệ chuyển đổi từ khách truy cập thành khách mua hàng
    private Double addToCartRate;    // Tỷ lệ thêm vào giỏ hàng
    private Double cartAbandonRate; // Tỷ lệ bỏ giỏ hàng
    private Double avgUnitsPTransaction; // Trung bình mỗi khách hàng mua bao nhiêu sản phẩm
    private Double avgOrderValue;     // Giá trị đơn hàng trung bình
    private Double returnRate;            // Tỷ lệ đổi trả
    private Double customerRetentionRate; // Tỷ lệ khách hàng quay lại
}