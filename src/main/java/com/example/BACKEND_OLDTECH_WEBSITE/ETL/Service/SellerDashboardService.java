package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.DTO.SellerDashboardDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SellerDashboardService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RefundRepository refundRepository;

    public SellerDashboardDTO getDashboardData(Integer sellerId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDate startDate = startDateTime.toLocalDate();
        LocalDate endDate = endDateTime.toLocalDate();
        // Doanh thu
        BigDecimal revenueRaw = orderRepository.getTotalRevenueBySellerAndDateRange(sellerId, startDate, endDate);
        Double revenue = revenueRaw != null ? revenueRaw.doubleValue() : 0.0;
        // Tổng số đơn
        Long totalOrders = orderRepository.getTotalOrdersBySellerAndDateRange(sellerId, startDate, endDate);
        // Giá trị đơn hàng trung bình
        Double averageOrderValue = (totalOrders != null && totalOrders > 0 && revenue != null) ? revenue / totalOrders : 0.0;
        // Số đơn đổi trả
        Long totalRefunds = refundRepository.getReturnOrdersCountBySellerAndDateRange(sellerId, startDate, endDate);
        // Tỷ lệ chuyển đổi (giả sử = 1 nếu có đơn, 0 nếu không, vì không có visits)
        Double conversionRate = (totalOrders != null && totalOrders > 0) ? 1.0 : 0.0;
        // Tỷ lệ khách hàng quay lại (giả sử = 0, cần bổ sung query nếu muốn tính đúng)
        Double repeatCustomerRate = 0.0;
        // Trend doanh thu theo ngày
        List<Map<String, Object>> revenueTrend = convertTrend(orderRepository.getDailyRevenueBySellerAndDateRange(sellerId, startDate, endDate), "orderDate", "revenue");
        // Trend số đơn theo ngày
        List<Map<String, Object>> orderTrend = convertTrend(orderRepository.getDailyOrdersBySellerAndDateRange(sellerId, startDate, endDate), "orderDate", "orderCount");
        // Trend đổi trả theo ngày
        List<Map<String, Object>> refundTrend = convertTrend(refundRepository.getDailyReturnsBySellerAndDateRange(sellerId, startDate, endDate), "refundDate", "returnCount");
        SellerDashboardDTO dto = new SellerDashboardDTO();
        dto.setRevenue(revenue);
        dto.setTotalOrders(totalOrders);
        dto.setAverageOrderValue(averageOrderValue);
        dto.setTotalRefunds(totalRefunds);
        dto.setConversionRate(conversionRate);
        dto.setRepeatCustomerRate(repeatCustomerRate);
        dto.setRevenueTrend(revenueTrend);
        dto.setOrderTrend(orderTrend);
        dto.setRefundTrend(refundTrend);
        dto.setConversionRateTrend(null);
        dto.setRepeatCustomerRateTrend(null);
        return dto;
    }

    // Chuyển List<Object[]> sang List<Map<String, Object>> cho DTO
    private List<Map<String, Object>> convertTrend(List<Object[]> raw, String labelKey, String valueKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (raw == null) return result;
        for (Object[] row : raw) {
            Map<String, Object> map = new HashMap<>();
            map.put(labelKey, row[0]);
            map.put(valueKey, row[1]);
            result.add(map);
        }
        return result;
    }
}
