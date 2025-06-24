package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.FactMonthlySales;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository.DataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Service tải dữ liệu - Bước cuối cùng trong quy trình ETL
 * Lưu dữ liệu đã transform vào data warehouse và gửi real-time events
 * REMOVED: Redis cache - không cần cho thị trường đồ công nghệ cũ volume thấp
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataLoaderService {    // Repository để lưu vào data warehouse
    private final DataWarehouseRepository dataWarehouseRepository;

    // ETL Event Producer for structured event publishing
    private final ETLEventProducer etlEventProducer;

    /**
     * Phương thức chính: Pure Kafka-based data loading
     * Architecture: Data Warehouse + Kafka Events (NO WebSocket)
     */
    @Transactional
    public void loadToDataWarehouse(TransformedData transformedData) {
        try {
            log.info("Bắt đầu tải dữ liệu cho ngày: {}", transformedData.getProcessDate());

            // Bước 1: Lưu vào data warehouse
            loadToFactTables(transformedData);

            // Bước 2: Publish tới Kafka để trigger consumers (NO WebSocket)
            publishToKafka(transformedData);
            log.info("Hoàn thành tải dữ liệu cho ngày: {}", transformedData.getProcessDate());

        } catch (Exception e) {
            log.error("Lỗi khi tải dữ liệu cho ngày: {}", transformedData.getProcessDate(), e);
            throw e;
        }
    }

    /**
     * Lưu dữ liệu vào các bảng fact (bảng chứa metrics chính)
     * Sử dụng upsert pattern: update nếu tồn tại, insert nếu chưa có
     */
    private void loadToFactTables(TransformedData data) {
        // Tạo record cho bảng FactMonthlySales
        FactMonthlySales factSales = createFactMonthlySales(data);

        // Kiểm tra dữ liệu đã tồn tại chưa (để avoid duplicate)
        dataWarehouseRepository.findByMonthKey(data.getProcessDate())
                .ifPresentOrElse(
                        existing -> {
                            // Update existing record với dữ liệu mới
                            updateFactMonthlySales(existing, data);
                            dataWarehouseRepository.save(existing);
                            log.info("Cập nhật dữ liệu sales fact hiện có cho ngày: {}", data.getProcessDate());
                        },
                        () -> {
                            // Insert new record
                            dataWarehouseRepository.save(factSales);
                            log.info("Tạo mới dữ liệu sales fact cho ngày: {}", data.getProcessDate());
                        }
                );
    }

    /**
     * Tạo bản ghi FactMonthlySales từ dữ liệu đã transform
     * Chuyên biệt cho thị trường công nghệ/đồ cũ
     */
    private FactMonthlySales createFactMonthlySales(TransformedData data) {
        FactMonthlySales factSales = new FactMonthlySales();

        TransformedData.SalesMetrics salesMetrics = data.getSalesMetrics();

        // Thiết lập các dimension keys
        factSales.setMonthKey(data.getProcessDate());
        factSales.setYearMonth(data.getProcessDate().toString().substring(0, 7)); // Format: "2025-06"

        // Các metrics chính cho business intelligence
        factSales.setTotalRevenue(salesMetrics.getTotalRevenue()); // Tổng doanh thu
        factSales.setTotalOrders(salesMetrics.getTotalOrders()); // Số đơn hàng
        factSales.setUniqueBuyers(salesMetrics.getUniqueCustomers()); // Số khách hàng unique
        factSales.setAverageOrderValue(salesMetrics.getAverageOrderValue()); // AOV
        factSales.setCancelledOrders(salesMetrics.getCancelledOrders()); // Đơn hủy
        factSales.setRefundAmount(salesMetrics.getRefundAmount()); // Tiền hoàn trả

        // Metrics đặc thù cho thị trường công nghệ cũ
        // Estimate: mỗi đơn hàng trung bình có 2 items công nghệ
        factSales.setTechItemsSold(salesMetrics.getTotalOrders() * 2);
        factSales.setEtlProcessedAt(java.time.LocalDateTime.now()); // Timestamp xử lý ETL

        return factSales;
    }

    /**
     * Cập nhật bản ghi FactMonthlySales hiện có với dữ liệu mới
     * Đảm bảo không làm mất dữ liệu cũ chưa được tổng hợp
     */
    private void updateFactMonthlySales(FactMonthlySales existing, TransformedData data) {
        TransformedData.SalesMetrics salesMetrics = data.getSalesMetrics();

        // Cập nhật các metrics chính
        existing.setTotalRevenue(salesMetrics.getTotalRevenue()); // Tổng doanh thu
        existing.setTotalOrders(salesMetrics.getTotalOrders()); // Số đơn hàng
        existing.setUniqueBuyers(salesMetrics.getUniqueCustomers()); // Số khách hàng unique
        existing.setAverageOrderValue(salesMetrics.getAverageOrderValue()); // AOV
        existing.setCancelledOrders(salesMetrics.getCancelledOrders()); // Đơn hủy
        existing.setRefundAmount(salesMetrics.getRefundAmount()); // Tiền hoàn trả        // Cập nhật metrics đặc thù cho thị trường công nghệ cũ
        existing.setTechItemsSold(salesMetrics.getTotalOrders() * 2); // Estimate: avg 2 items per order
        existing.setEtlProcessedAt(java.time.LocalDateTime.now()); // Timestamp xử lý ETL
    }

    /**
     * Đăng tải sự kiện đến Kafka cho cập nhật dashboard thời gian thực
     */
    private void publishToKafka(TransformedData data) {
        try {
            log.info("Publishing ETL events to Kafka for date: {}", data.getProcessDate());

            // Publish sales metrics using producer service
            Map<String, Object> salesData = convertSalesMetricsToMap(data.getSalesMetrics());
            etlEventProducer.publishSalesMetricsEvent(salesData);

            // Publish business KPIs using producer service
            Map<String, Object> kpiData = convertBusinessKPIsToMap(data.getBusinessKPIs());
            etlEventProducer.publishBusinessKPIsEvent(kpiData);

            // Publish data quality alerts if any
            if (!data.getDataQualityAlerts().isEmpty()) {
                for (TransformedData.AlertDefinition alert : data.getDataQualityAlerts()) {
                    Map<String, Object> alertData = new HashMap<>();
                    alertData.put("level", alert.getSeverity());
                    alertData.put("message", alert.getMessage());
                    alertData.put("component", alert.getAlertType());
                    alertData.put("date", data.getProcessDate().toString());
                    alertData.put("context", alert.getContext());

                    etlEventProducer.publishDataQualityAlert(alertData);
                }
            }

            // Publish ETL completion status
            Map<String, Object> completionSummary = new HashMap<>();
            completionSummary.put("processDate", data.getProcessDate().toString());
            completionSummary.put("recordsProcessed", data.getSalesMetrics().getTotalOrders());
            completionSummary.put("dataQualityScore", data.getDataQualityAlerts().isEmpty() ? 100.0 : 85.0);

            etlEventProducer.publishETLCompletionEvent(
                    data.getProcessDate().toString(),
                    "SUCCESS",
                    0L, // Duration will be calculated in orchestrator
                    completionSummary
            );

            log.info("Successfully published all ETL events to Kafka for date: {}", data.getProcessDate());

        } catch (Exception e) {
            log.error("Error publishing ETL events to Kafka for date: {}", data.getProcessDate(), e);
            // ETL should continue even if Kafka publishing fails
        }
    }

    /**
     * Convert SalesMetrics to Map for Kafka event
     */
    private Map<String, Object> convertSalesMetricsToMap(TransformedData.SalesMetrics salesMetrics) {
        Map<String, Object> map = new HashMap<>();
        map.put("totalRevenue", salesMetrics.getTotalRevenue());
        map.put("totalOrders", salesMetrics.getTotalOrders());
        map.put("uniqueCustomers", salesMetrics.getUniqueCustomers());
        map.put("averageOrderValue", salesMetrics.getAverageOrderValue());
        map.put("revenueByCategory", salesMetrics.getRevenueByCategory());
        map.put("ordersByStatus", salesMetrics.getOrdersByStatus());
        map.put("growthRate", salesMetrics.getGrowthRate());
        map.put("cancelledOrders", salesMetrics.getCancelledOrders());
        map.put("refundAmount", salesMetrics.getRefundAmount());
        return map;
    }

    /**
     * Convert BusinessKPIs to Map for Kafka event
     */
    private Map<String, Object> convertBusinessKPIsToMap(TransformedData.BusinessKPIs businessKPIs) {
        Map<String, Object> map = new HashMap<>();
        map.put("customerAcquisitionCost", businessKPIs.getCustomerAcquisitionCost());
        map.put("customerLifetimeValue", businessKPIs.getCustomerLifetimeValue());
        map.put("churnRate", businessKPIs.getChurnRate());
        map.put("conversionRate", businessKPIs.getConversionRate());
        map.put("newCustomers", businessKPIs.getNewCustomers());
        map.put("returningCustomers", businessKPIs.getReturningCustomers());
        map.put("monthlyRecurringRevenue", businessKPIs.getMonthlyRecurringRevenue());
        map.put("orderFulfillmentRate", businessKPIs.getOrderFulfillmentRate());        return map;
    }    /**
     * Lấy sales metrics từ database (thay thế Redis cache)
     */
    public TransformedData.SalesMetrics getSalesMetrics(LocalDate date) {
        try {
            FactMonthlySales fact = dataWarehouseRepository.findByProcessDate(date);

            if (fact == null) {
                return null;
            }
            
            TransformedData.SalesMetrics metrics = new TransformedData.SalesMetrics();
            metrics.setTotalRevenue(fact.getTotalRevenue());
            metrics.setTotalOrders(fact.getTotalOrders());
            metrics.setUniqueCustomers(fact.getUniqueBuyers());
            metrics.setAverageOrderValue(fact.getAverageOrderValue());

            return metrics;

        } catch (Exception e) {
            log.error("Lỗi khi lấy sales metrics cho ngày: {}", date, e);
            return null;
        }
    }

    /**
     * Lấy metrics mới nhất từ database (thay thế Redis cache)
     */
    public Map<String, Object> getLatestMetrics() {
        try {
            // Lấy record mới nhất từ data warehouse
            FactMonthlySales latestFact = dataWarehouseRepository.findTopByOrderByProcessDateDesc();

            if (latestFact == null) {
                return new HashMap<>();
            }
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalRevenue", latestFact.getTotalRevenue());
            metrics.put("totalOrders", latestFact.getTotalOrders());
            metrics.put("uniqueCustomers", latestFact.getUniqueBuyers());
            metrics.put("averageOrderValue", latestFact.getAverageOrderValue());
            metrics.put("lastUpdated", latestFact.getMonthKey());
            metrics.put("source", "DATABASE"); // Thay vì "REDIS_CACHE"

            return metrics;

        } catch (Exception e) {
            log.error("Lỗi khi lấy metrics mới nhất từ database", e);
            return new HashMap<>();
        }
    }
}
