package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.ETLOrchestrator;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.DataLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller chính cho hệ thống ETL - Quản lý pipeline ETL thông thường
 * 
 * CHỨC NĂNG CHÍNH:
 * 1. Trigger manual ETL jobs (chạy ETL thủ công)
 * 2. Monitor pipeline status (theo dõi trạng thái)
 * 3. Health checks (kiểm tra sức khỏe hệ thống)
 * 4. Lấy metrics real-time cho dashboard
 * 
 * PHỤC VỤ CHO:
 * - Admin dashboard monitoring
 * - Scheduled ETL job management
 * - Daily/hourly data processing
 * - Real-time analytics
 * 
 * BẢO MẬT: Chỉ Admin mới được trigger ETL jobs
 */
@RestController
@RequestMapping("/api/etl")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "etl.enabled", havingValue = "true", matchIfMissing = true)
public class ETLController {

    private final ETLOrchestrator etlOrchestrator;
    private final DataLoaderService dataLoaderService;

    /**
     * API 1: Chạy ETL cho ngày cụ thể
     */
    @PostMapping("/run/{date}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> runETLForDate(@PathVariable String date) {
        LocalDate processDate = LocalDate.parse(date);
        log.info("Admin trigger ETL thủ công cho ngày: {}", processDate);
        
        return executeETLOperation(() -> {
            ETLOrchestrator.ETLResult result = etlOrchestrator.runDailyETL(processDate);
            return Map.of(
                "status", "SUCCESS",
                "message", "ETL pipeline hoàn tất thành công",
                "result", result
            );
        });
    }

    /**
     * API 2: Chạy ETL cho ngày hôm nay
     */
    @PostMapping("/run/today")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> runETLForToday() {
        log.info("Admin trigger ETL cho hôm nay");
        
        return executeETLOperation(() -> {
            ETLOrchestrator.ETLResult result = etlOrchestrator.runCurrentDayETL();
            return Map.of(
                "status", "SUCCESS",
                "message", "ETL pipeline hoàn tất cho hôm nay",
                "result", result
            );
        });
    }

    /**
     * API 3: Chạy ETL cho hôm qua
     */
    @PostMapping("/run/yesterday")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> runETLForYesterday() {
        log.info("Admin trigger ETL cho hôm qua");
        
        return executeETLOperation(() -> {
            ETLOrchestrator.ETLResult result = etlOrchestrator.runYesterdayETL();
            return Map.of(
                "status", "SUCCESS",
                "message", "ETL pipeline hoàn tất cho hôm qua",
                "result", result
            );
        });
    }

    /**
     * API 4: Kiểm tra trạng thái pipeline
     */
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getPipelineStatus() {
        return executeETLOperation(() -> etlOrchestrator.getCurrentPipelineStatus());
    }

    /**
     * API 5: Lấy metrics mới nhất cho dashboard
     */
    @GetMapping("/metrics/latest")
    public ResponseEntity<?> getLatestMetrics() {
        return executeETLOperation(() -> {
            Map<String, Object> metrics = dataLoaderService.getLatestMetrics();
            
            if (metrics.isEmpty()) {
                return Map.of(
                    "message", "Chưa có metrics gần đây",
                    "status", "NO_DATA"
                );
            }
            
            return metrics;
        });
    }

    /**
     * Lấy dữ liệu mới nhất để hiển thị trên dashboard
     */
    @GetMapping("/metrics/sales/{date}")
    public ResponseEntity<?> getSalesMetrics(@PathVariable String date) {
        LocalDate requestDate = LocalDate.parse(date);
        
        return executeETLOperation(() -> {
            var salesMetrics = dataLoaderService.getSalesMetrics(requestDate);
            
            if (salesMetrics == null) {
                return Map.of(
                    "message", "No metrics available for date: " + date,
                    "status", "NO_DATA"
                );
            }
            
            return salesMetrics;
        });
    }

    /**
     * Endpoint kiểm tra tình trạng pipeline ETL
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "HEALTHY");
            health.put("timestamp", System.currentTimeMillis());
            health.put("pipeline", etlOrchestrator.getCurrentPipelineStatus());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UNHEALTHY");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    /**
     * Lấy thông tin pipeline ETL
     */
    @GetMapping("/info")
    public ResponseEntity<?> getETLInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "OldTech E-commerce ETL Pipeline");
        info.put("version", "1.0.0");
        info.put("description", "Extract, Transform, Load pipeline for e-commerce analytics");
        
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("extraction", "Orders, Customers, Products, Sellers");
        capabilities.put("transformation", "Sales metrics, Customer segmentation, Business KPIs");
        capabilities.put("loading", "Data warehouse, Kafka events");
        capabilities.put("scheduling", "Daily at 1:00 AM, Hourly for real-time");
        capabilities.put("monitoring", "Data quality checks, Real-time alerts");
        
        info.put("capabilities", capabilities);
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("manual_trigger", "POST /api/etl/run/{date}");
        endpoints.put("status", "GET /api/etl/status");
        endpoints.put("metrics", "GET /api/etl/metrics/latest");
        endpoints.put("health", "GET /api/etl/health");
        
        info.put("endpoints", endpoints);
        
        return ResponseEntity.ok(info);
    }

    /**
     * Helper method để xử lý các ETL operations với error handling thống nhất
     */
    private ResponseEntity<?> executeETLOperation(ETLOperation operation) {
        try {
            Object result = operation.execute();
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("ETL operation failed", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", "ETL pipeline thất bại: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @FunctionalInterface
    private interface ETLOperation {
        Object execute() throws Exception;
    }
}
