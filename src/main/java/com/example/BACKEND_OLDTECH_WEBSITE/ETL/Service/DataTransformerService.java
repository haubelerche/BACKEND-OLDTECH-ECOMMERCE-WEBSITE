package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData.SalesMetrics;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData.CustomerSegments;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData.ProductAnalytics;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData.BusinessKPIs;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class DataTransformerService {
    public TransformedData transform(ExtractedData data) {
        TransformedData result = new TransformedData();
        result.setProcessDate(data.getExtractionDate());
        // Sales Metrics
        SalesMetrics salesMetrics = new SalesMetrics();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrders = 0;
        Set<Integer> uniqueCustomers = new HashSet<>();
        Map<String, BigDecimal> revenueByCategory = new HashMap<>();
        Map<String, Integer> ordersByStatus = new HashMap<>();
        int cancelledOrders = 0;
        BigDecimal refundAmount = BigDecimal.ZERO;
        if (data.getOrders() != null) {
            for (ExtractedData.OrderData order : data.getOrders()) {
                totalOrders++;
                uniqueCustomers.add(order.getUserId());
                totalRevenue = totalRevenue.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
                // Revenue by category
                if (order.getProductCategory() != null) {
                    revenueByCategory.put(order.getProductCategory(),
                        revenueByCategory.getOrDefault(order.getProductCategory(), BigDecimal.ZERO).add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO));
                }
                // Orders by status
                if (order.getStatus() != null) {
                    ordersByStatus.put(order.getStatus(), ordersByStatus.getOrDefault(order.getStatus(), 0) + 1);
                    if (order.getStatus().equalsIgnoreCase("CANCELLED")) cancelledOrders++;
                }
            }
        }
        salesMetrics.setTotalRevenue(totalRevenue);
        salesMetrics.setTotalOrders(totalOrders);
        salesMetrics.setUniqueCustomers(uniqueCustomers.size());
        salesMetrics.setAverageOrderValue(totalOrders > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        salesMetrics.setRevenueByCategory(revenueByCategory);
        salesMetrics.setOrdersByStatus(ordersByStatus);
        salesMetrics.setGrowthRate(BigDecimal.ZERO); // TODO: implement if needed
        salesMetrics.setCancelledOrders(cancelledOrders);
        salesMetrics.setRefundAmount(refundAmount); // TODO: implement if needed
        result.setSalesMetrics(salesMetrics);
        // Customer Segments (giả lập)
        CustomerSegments customerSegments = new CustomerSegments();
        customerSegments.setSegments(new ArrayList<>());
        customerSegments.setRetentionRates(new HashMap<>());
        customerSegments.setLifetimeValues(new HashMap<>());
        result.setCustomerSegments(customerSegments);
        // Product Analytics (giả lập)
        result.setProductAnalytics(new ProductAnalytics());
        // Business KPIs (giả lập)
        result.setBusinessKPIs(new BusinessKPIs());
        // Data Quality Alerts (giữ nguyên nếu có)
        result.setDataQualityAlerts(new ArrayList<>());
        return result;
    }
}
