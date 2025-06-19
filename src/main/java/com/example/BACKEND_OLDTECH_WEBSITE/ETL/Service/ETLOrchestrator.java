package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Dịch vụ điều phối ETL - Quản lý toàn bộ quy trình Extract-Transform-Load
 * Chịu trách nhiệm điều phối các bước ETL theo thứ tự và xử lý lỗi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ETLOrchestrator {

    // Tiêm các service con để thực hiện từng bước ETL
    private final DataExtractorService extractorService; // Bước Extract
    private final DataTransformerService transformerService; // Bước Transform  
    private final DataLoaderService loaderService; // Bước Load
    private final DataQualityMonitoringService qualityMonitoringService; // Kiểm tra chất lượng

    /**
     * Chạy toàn bộ quy trình ETL cho một ngày cụ thể
     * Bao gồm: Extract -> Quality Check -> Transform -> Load
     */
    public ETLResult runDailyETL(LocalDate processDate) {
        log.info("Bắt đầu quy trình ETL cho ngày: {}", processDate);
        
        // Khởi tạo object theo dõi kết quả ETL
        ETLResult result = new ETLResult();
        result.setProcessDate(processDate);
        result.setStartTime(LocalDateTime.now());
        
        try {
            // Bước 1: Trích xuất dữ liệu từ các nguồn (orders, products, users)
            log.info("Bước 1: Bắt đầu trích xuất dữ liệu...");
            ExtractedData extractedData = extractorService.extractDailyData(processDate);
            result.setRecordsExtracted(extractedData.getOrders().size());
            
            // Bước 2: Kiểm tra chất lượng dữ liệu (validate, check missing data)
            log.info("Bước 2: Kiểm tra chất lượng dữ liệu...");
            qualityMonitoringService.validateDataQuality(extractedData);
            result.setDataQualityScore(extractedData.getQualityReport().getQualityScore());
            
            // Bước 3: Biến đổi dữ liệu (tính toán metrics, phân khúc khách hàng)
            log.info("Bước 3: Bắt đầu biến đổi dữ liệu...");
            TransformedData transformedData = transformerService.transform(extractedData);
            result.setRecordsTransformed(transformedData.getSalesMetrics().getTotalOrders());
            
            // Bước 4: Tải dữ liệu vào data warehouse để phân tích
            log.info("Bước 4: Tải dữ liệu vào kho dữ liệu...");
            loaderService.loadToDataWarehouse(transformedData);
            result.setRecordsLoaded(transformedData.getSalesMetrics().getTotalOrders());
            
            // Hoàn thành thành công - ghi log kết quả
            result.setEndTime(LocalDateTime.now());
            result.setStatus("SUCCESS");
            result.setDuration(calculateDuration(result.getStartTime(), result.getEndTime()));
            
            log.info("Quy trình ETL hoàn tất thành công cho ngày: {} trong {} giây", 
                    processDate, result.getDuration());
            
            return result;
            
        } catch (Exception e) {
            // Xử lý lỗi và ghi log chi tiết
            result.setEndTime(LocalDateTime.now());
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
            result.setDuration(calculateDuration(result.getStartTime(), result.getEndTime()));
            
            log.error("Quy trình ETL thất bại cho ngày: {}", processDate, e);
            throw new ETLException("Quy trình ETL thất bại cho ngày: " + processDate, e);
        }
    }

    /**
     * Shortcut: Chạy ETL cho ngày hôm nay
     */
    public ETLResult runCurrentDayETL() {
        return runDailyETL(LocalDate.now());
    }

    /**
     * Shortcut: Chạy ETL cho hôm qua (use case phổ biến)
     * Thường dùng vào sáng sớm để xử lý dữ liệu ngày hôm trước
     */
    public ETLResult runYesterdayETL() {
        return runDailyETL(LocalDate.now().minusDays(1));
    }

    /**
     * Lấy trạng thái hiện tại của toàn bộ pipeline
     * Dùng cho monitoring và dashboard
     */
    public Map<String, Object> getCurrentPipelineStatus() {
        Map<String, Object> status = new HashMap<>();
          try {
            // Lấy chỉ số thời gian thực từ database (thay vì Redis cache)
            Map<String, Object> latestMetrics = loaderService.getLatestMetrics();
            status.put("latestMetrics", latestMetrics);
            status.put("status", "RUNNING");
            status.put("lastUpdate", LocalDateTime.now());
            
            // Thêm chỉ số sức khỏe của pipeline
            status.put("pipelineHealth", checkPipelineHealth());
            
        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
            log.error("Error getting pipeline status", e);
        }
        
        return status;
    }

    /**
     * Kiểm tra sức khỏe của pipeline
     */
    private Map<String, Object> checkPipelineHealth() {
        Map<String, Object> health = new HashMap<>();
          // Kiểm tra xem có dữ liệu gần đây không
        try {
            Map<String, Object> latestMetrics = loaderService.getLatestMetrics();
            boolean hasRecentData = latestMetrics != null && !latestMetrics.isEmpty();
            
            health.put("hasRecentData", hasRecentData);
            health.put("dataFreshness", hasRecentData ? "GOOD" : "STALE");
            health.put("lastSuccessfulRun", LocalDateTime.now().minusHours(1)); // Simulated
            
        } catch (Exception e) {
            health.put("status", "UNHEALTHY");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    /**
     * Tính thời gian giữa hai thời điểm
     */
    private long calculateDuration(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).getSeconds();
    }

    /**
     * Lớp ETLResult để theo dõi thực thi pipeline
     */
    public static class ETLResult {
        private LocalDate processDate;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private String errorMessage;
        private long duration; // in seconds
        private int recordsExtracted;
        private int recordsTransformed;
        private int recordsLoaded;
        private double dataQualityScore;

     
        public LocalDate getProcessDate() { return processDate; }
        public void setProcessDate(LocalDate processDate) { this.processDate = processDate; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }

        public int getRecordsExtracted() { return recordsExtracted; }
        public void setRecordsExtracted(int recordsExtracted) { this.recordsExtracted = recordsExtracted; }

        public int getRecordsTransformed() { return recordsTransformed; }
        public void setRecordsTransformed(int recordsTransformed) { this.recordsTransformed = recordsTransformed; }

        public int getRecordsLoaded() { return recordsLoaded; }
        public void setRecordsLoaded(int recordsLoaded) { this.recordsLoaded = recordsLoaded; }

        public double getDataQualityScore() { return dataQualityScore; }
        public void setDataQualityScore(double dataQualityScore) { this.dataQualityScore = dataQualityScore; }
    }

    /**
     * Ngoại lệ 
     */
    public static class ETLException extends RuntimeException {
        public ETLException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
