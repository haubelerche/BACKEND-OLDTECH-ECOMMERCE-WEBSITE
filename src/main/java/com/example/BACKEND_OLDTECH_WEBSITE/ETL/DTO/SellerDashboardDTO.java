package com.example.BACKEND_OLDTECH_WEBSITE.ETL.DTO;

import java.util.List;
import java.util.Map;

public class SellerDashboardDTO {
    private Double revenue;
    private Long totalOrders;
    private Double averageOrderValue;
    private Long totalRefunds;
    private Double conversionRate;
    private Double repeatCustomerRate;
    private List<Map<String, Object>> revenueTrend;
    private List<Map<String, Object>> orderTrend;
    private List<Map<String, Object>> refundTrend;
    private List<Map<String, Object>> conversionRateTrend;
    private List<Map<String, Object>> repeatCustomerRateTrend;

    // Getters and setters
    public Double getRevenue() { return revenue; }
    public void setRevenue(Double revenue) { this.revenue = revenue; }
    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
    public Double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    public Long getTotalRefunds() { return totalRefunds; }
    public void setTotalRefunds(Long totalRefunds) { this.totalRefunds = totalRefunds; }
    public Double getConversionRate() { return conversionRate; }
    public void setConversionRate(Double conversionRate) { this.conversionRate = conversionRate; }
    public Double getRepeatCustomerRate() { return repeatCustomerRate; }
    public void setRepeatCustomerRate(Double repeatCustomerRate) { this.repeatCustomerRate = repeatCustomerRate; }
    public List<Map<String, Object>> getRevenueTrend() { return revenueTrend; }
    public void setRevenueTrend(List<Map<String, Object>> revenueTrend) { this.revenueTrend = revenueTrend; }
    public List<Map<String, Object>> getOrderTrend() { return orderTrend; }
    public void setOrderTrend(List<Map<String, Object>> orderTrend) { this.orderTrend = orderTrend; }
    public List<Map<String, Object>> getRefundTrend() { return refundTrend; }
    public void setRefundTrend(List<Map<String, Object>> refundTrend) { this.refundTrend = refundTrend; }
    public List<Map<String, Object>> getConversionRateTrend() { return conversionRateTrend; }
    public void setConversionRateTrend(List<Map<String, Object>> conversionRateTrend) { this.conversionRateTrend = conversionRateTrend; }
    public List<Map<String, Object>> getRepeatCustomerRateTrend() { return repeatCustomerRateTrend; }
    public void setRepeatCustomerRateTrend(List<Map<String, Object>> repeatCustomerRateTrend) { this.repeatCustomerRateTrend = repeatCustomerRateTrend; }
}
