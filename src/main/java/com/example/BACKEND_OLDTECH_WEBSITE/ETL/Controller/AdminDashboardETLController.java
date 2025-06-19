package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.AdminDashboardETLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controller để quản lý ETL process cho Admin Dashboard
 * Chỉ admin mới có quyền truy cập
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard-etl")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminDashboardETLController {

    private final AdminDashboardETLService etlService;

    /**
     * Chạy ETL cho một ngày cụ thể
     */
    @PostMapping("/run")
    public ResponseEntity<?> runETLForDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        LocalDate targetDate = date != null ? date : LocalDate.now().minusDays(1);
        log.info("Admin triggered admin dashboard ETL for date: {}", targetDate);
        
        return executeETLWithErrorHandling(() -> {
            etlService.runETLForDate(targetDate);
            return Map.of(
                "success", true,
                "message", "Admin dashboard ETL completed successfully",
                "date", targetDate,
                "timestamp", System.currentTimeMillis()
            );
        }, "Admin dashboard ETL failed for date");
    }

    /**
     * Chạy backfill ETL cho nhiều ngày
     */
    @PostMapping("/backfill")
    public ResponseEntity<?> runBackfillETL(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "Start date must be before end date"
                ));
        }
        
        log.info("Admin triggered admin dashboard backfill ETL from {} to {}", startDate, endDate);
        
        return executeETLWithErrorHandling(() -> {
            etlService.runBackfillETL(startDate, endDate);
            return Map.of(
                "success", true,
                "message", "Admin dashboard backfill ETL completed",
                "startDate", startDate,
                "endDate", endDate,
                "timestamp", System.currentTimeMillis()
            );
        }, "Admin dashboard backfill ETL failed");
    }

    /**
     * Force chạy daily ETL ngay lập tức
     */
    @PostMapping("/force-daily")
    public ResponseEntity<?> forceDailyETL() {
        log.info("Admin forced admin dashboard daily ETL execution");
        
        return executeETLWithErrorHandling(() -> {
            etlService.runDailyETL();
            return Map.of(
                "success", true,
                "message", "Admin dashboard daily ETL process completed successfully",
                "timestamp", System.currentTimeMillis()
            );
        }, "Forced admin dashboard daily ETL failed");
    }

    /**
     * Lấy trạng thái ETL process
     */
    @GetMapping("/status")
    public ResponseEntity<?> getETLStatus() {
        return executeETLWithErrorHandling(() -> {
            return Map.of(
                "success", true,
                "status", "Admin dashboard ETL service is running",
                "scheduler", "Daily at 1:00 AM (before seller dashboard)",
                "lastRun", "Check application logs",
                "nextRun", "Next day at 1:00 AM",
                "description", "Platform-wide metrics ETL for admin dashboard",
                "timestamp", System.currentTimeMillis()
            );
        }, "Failed to get admin dashboard ETL status");
    }

    /**
     * Test connectivity và validate data
     */
    @GetMapping("/health-check")
    public ResponseEntity<?> healthCheck() {
        return executeETLWithErrorHandling(() -> {
            LocalDate testDate = LocalDate.now().minusDays(1);
            etlService.processAdminDashboardForDate(testDate, "DAILY");
            
            return Map.of(
                "success", true,
                "message", "Admin dashboard ETL health check passed",
                "testDate", testDate,
                "databaseConnection", "OK",
                "calculations", "OK",
                "timestamp", System.currentTimeMillis()
            );
        }, "Admin dashboard ETL health check failed");
    }

    /**
     * Helper method để xử lý lỗi chung cho tất cả ETL operations
     */
    private ResponseEntity<?> executeETLWithErrorHandling(ETLOperation operation, String errorMessage) {
        try {
            Map<String, Object> result = operation.execute();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(errorMessage, e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", errorMessage + ": " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ));
        }
    }

    @FunctionalInterface
    private interface ETLOperation {
        Map<String, Object> execute() throws Exception;
    }
}
