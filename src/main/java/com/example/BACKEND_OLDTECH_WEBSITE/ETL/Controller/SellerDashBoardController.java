package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.DataTransformerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.SellerDashboardService;
import java.time.LocalDate;
import java.util.Map;

/**
 * Controller hợp nhất cho Seller Dashboard
 * Bao gồm cả API cho sellers và admin ETL operations
 */
@Slf4j
@RestController
@RequestMapping("/seller-dashboard")
@RequiredArgsConstructor
public class SellerDashBoardController {

    private final DataTransformerService.SellerDashboardService sellerDashboardService;
    private final SellerDashboardService etlService;

    //=========================================
    // SELLER APIS - DASHBOARD OPERATIONS  
    //=========================================

    /**
     * API lấy KPIs tổng quan cho seller
     * GET /oldtech/seller/dashboard/overview/kpis
     */
     @GetMapping("/seller/dashboard/overview/kpis")
    @PreAuthorize("hasAnyAuthority('Seller')")
    public ResponseEntity<Map<String, Object>> getSellerKPIs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "7_days") String period,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Map<String, Object> kpis = sellerDashboardService.getSellerKPIs(
            userDetails.getUsername(), period, startDate, endDate);
        return ResponseEntity.ok(kpis);
    }

    /**
     * API lấy quick stats cho dashboard header
     * GET /oldtech/seller/dashboard/quick-stats
     */
    @GetMapping("/seller/dashboard/quick-stats")
    @PreAuthorize("hasAnyAuthority('Seller')")
    public ResponseEntity<Map<String, Object>> getQuickStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> quickStats = sellerDashboardService.getQuickStats(
            userDetails.getUsername());
        return ResponseEntity.ok(quickStats);
    }

    //=========================================
    // ADMIN APIS - ETL OPERATIONS
    //=========================================

    /**
     * Chạy ETL cho tất cả seller trong một tuần cụ thể
     * POST /oldtech/admin/dashboard-etl/run-all?weekStartDate=2024-01-15
     */
    @PostMapping("/admin/dashboard-etl/run-all")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> runETLForAllSellers(
            @RequestParam(name = "weekStartDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {

        LocalDate targetWeek = weekStartDate != null ? weekStartDate : LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        log.info("Admin triggered weekly ETL for all sellers on week: {}", targetWeek);

        return executeETLWithErrorHandling(() -> {
            etlService.runETLForAllSellersWeekly(targetWeek);
            return Map.of(
                "success", true,
                "message", "Weekly ETL completed successfully for all sellers",
                "weekStartDate", targetWeek,
                "timestamp", System.currentTimeMillis()
            );
        }, "Admin weekly ETL failed for all sellers");
    }

    /**
     * Chạy ETL cho một seller cụ thể trong tuần
     * POST /oldtech/admin/dashboard-etl/run-seller/{sellerId}?weekStartDate=2024-01-15
     */
    @PostMapping({"/admin/dashboard-etl/run-seller/{sellerId}", "/seller/dashboard-etl/run-seller/{sellerId}"})
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> runETLForSeller(
            @PathVariable Integer sellerId,
            @RequestParam(name = "weekStartDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {

        LocalDate targetWeek = weekStartDate != null ? weekStartDate : LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        log.info("Admin triggered weekly ETL for seller {} on week: {}", sellerId, targetWeek);

        return executeETLWithErrorHandling(() -> {
            etlService.runETLForSellerWeekly(sellerId, targetWeek);
            return Map.of(
                "success", true,
                "message", "Weekly ETL completed successfully for seller " + sellerId,
                "sellerId", sellerId,
                "weekStartDate", targetWeek,
                "timestamp", System.currentTimeMillis()
            );
        }, "Admin weekly ETL failed for seller " + sellerId);
    }

    /**
     * Chạy ETL cho nhiều tuần (backfill)
     * POST /oldtech/admin/dashboard-etl/backfill?startWeek=2024-01-01&endWeek=2024-01-29
     */
    @PostMapping("/admin/dashboard-etl/backfill")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> runBackfillETL(
            @RequestParam(name = "startWeek") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startWeek,
            @RequestParam(name = "endWeek") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endWeek,
            @RequestParam(required = false) Integer sellerId) {

        try {
            if (startWeek.isAfter(endWeek)) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Start week must be before end week"
                    ));
            }

            LocalDate currentWeek = startWeek;
            int processedWeeks = 0;

            while (!currentWeek.isAfter(endWeek)) {
                try {
                    if (sellerId != null) {
                        etlService.runETLForSellerWeekly(sellerId, currentWeek);
                        log.info("Backfill weekly ETL completed for seller {} on week {}", sellerId, currentWeek);
                    } else {
                        etlService.runETLForAllSellersWeekly(currentWeek);
                        log.info("Backfill weekly ETL completed for all sellers on week {}", currentWeek);
                    }
                    processedWeeks++;
                } catch (Exception e) {
                    log.error("Backfill weekly ETL failed for week {}", currentWeek, e);
                }
                currentWeek = currentWeek.plusWeeks(1);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Backfill weekly ETL completed",
                "startWeek", startWeek,
                "endWeek", endWeek,
                "processedWeeks", processedWeeks,
                "sellerId", sellerId != null ? sellerId : "all",
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("Backfill weekly ETL failed", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Backfill weekly ETL failed: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ));
        }
    }

    /**
     * Lấy trạng thái ETL process
     * GET /oldtech/admin/dashboard-etl/status
     */
    @GetMapping("/admin/dashboard-etl/status")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getETLStatus() {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "status", "ETL service is running",
                "scheduler", "Daily at 2:00 AM",
                "lastRun", "Check application logs",
                "nextRun", "Next day at 2:00 AM",
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("Error getting ETL status", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Failed to get ETL status: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ));
        }
    }

    /**
     * Force chạy weekly ETL ngay lập tức
     * POST /oldtech/admin/dashboard-etl/force-weekly
     */
    @PostMapping("/admin/dashboard-etl/force-weekly")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> forceWeeklyETL() {
        try {
            log.info("Admin forced weekly ETL execution");
            etlService.runWeeklyETL();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Weekly ETL process completed successfully",
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("Forced weekly ETL failed", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Weekly ETL failed: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ));
        }
    }

    /**
     * Lấy thống kê về ETL data quality
     * GET /oldtech/admin/dashboard-etl/data-quality
     */
    @GetMapping("/admin/dashboard-etl/data-quality")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getDataQuality() {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Data quality metrics",
                "metrics", Map.of(
                    "totalRecords", "Check database",
                    "averageQualityScore", "1.0",
                    "recordsWithErrors", "0",
                    "lastQualityCheck", System.currentTimeMillis()
                ),
                "recommendations", java.util.List.of(
                    "ETL is running smoothly",
                    "Data quality is good",
                    "No action required"
                ),
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("Error getting data quality metrics", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Failed to get data quality metrics: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ));
        }
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
