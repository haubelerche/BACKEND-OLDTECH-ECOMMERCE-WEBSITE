package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevenueStatisticsDTO {
    private BigDecimal totalRevenue; // Tổng doanh thu
    private BigDecimal avgOrderValue; // Giá trị đơn hàng trung bình
    private BigDecimal totalRefunds; // Tổng số tiền hoàn lại
    private BigDecimal totalProfit; // Tổng lợi nhuận
    private BigDecimal totalCost; // Tổng chi phí
    private BigDecimal totalSales; // Tổng số lượng loaij sản phẩm đã bán
    private BigDecimal totalOrders; // Tổng số đơn hàng
    private BigDecimal totalProducts; // Tổng số sản phẩm



}