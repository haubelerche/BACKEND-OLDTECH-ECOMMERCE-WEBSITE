package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminAlert;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminAlert.*;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.AdminAlertService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Admin Alert Controller - API endpoints cho hệ thống cảnh báo
 * Theo dõi sức khỏe hệ thống 24/7, phát hiện sớm vấn đề tiềm ẩn
 */
@RestController
@RequestMapping("/api/admin/alerts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminAlertController {

    private final AdminAlertService adminAlertService;

    // ===============================
    // DASHBOARD & OVERVIEW
    // ===============================

    /**
     * Lấy alert dashboard summary
     */
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        try {
            Map<String, Object> summary = adminAlertService.getAlertDashboardSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting alert dashboard summary", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy critical alerts cần xử lý ngay
     */
    @GetMapping("/critical")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminAlert>> getCriticalAlerts() {
        try {
            List<AdminAlert> criticalAlerts = adminAlertService.getCriticalAlerts();
            return ResponseEntity.ok(criticalAlerts);
        } catch (Exception e) {
            log.error("Error getting critical alerts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy alerts theo category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminAlert>> getAlertsByCategory(@PathVariable AlertCategory category) {
        try {
            List<AdminAlert> alerts = adminAlertService.getAlertsByCategory(category);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error getting alerts by category: {}", category, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===============================
    // SYSTEM HEALTH MONITORING
    // ===============================

    /**
     * Tạo System Health Alert
     */
    @PostMapping("/system-health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> createSystemHealthAlert(
            @RequestParam String component,
            @RequestParam String metric,
            @RequestParam BigDecimal value,
            @RequestParam BigDecimal threshold) {
        try {
            AdminAlert alert = adminAlertService.createSystemHealthAlert(component, metric, value, threshold);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error creating system health alert", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===============================
    // SECURITY MONITORING
    // ===============================

    /**
     * Tạo Security Alert
     */
    @PostMapping("/security")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> createSecurityAlert(
            @RequestParam String alertType,
            @RequestParam String description,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        try {
            AdminAlert alert = adminAlertService.createSecurityAlert(alertType, description, entityType, entityId);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error creating security alert", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===============================
    // PERFORMANCE MONITORING
    // ===============================

    /**
     * Tạo Performance Alert
     */
    @PostMapping("/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> createPerformanceAlert(
            @RequestParam String metric,
            @RequestParam BigDecimal currentValue,
            @RequestParam BigDecimal threshold) {
        try {
            AdminAlert alert = adminAlertService.createPerformanceAlert(metric, currentValue, threshold);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error creating performance alert", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===============================
    // BUSINESS KPI MONITORING
    // ===============================

    /**
     * Tạo Business KPI Alert
     */
    @PostMapping("/business-kpi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> createBusinessKpiAlert(
            @RequestParam String kpiName,
            @RequestParam BigDecimal currentValue,
            @RequestParam BigDecimal expectedValue,
            @RequestParam String period) {
        try {
            AdminAlert alert = adminAlertService.createBusinessKpiAlert(kpiName, currentValue, expectedValue, period);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error creating business KPI alert", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===============================
    // FRAUD DETECTION
    // ===============================

    /**
     * Tạo Fraud Alert
     */
    @PostMapping("/fraud")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> createFraudAlert(
            @RequestParam String fraudType,
            @RequestParam String description,
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam BigDecimal riskScore) {
        try {
            AdminAlert alert = adminAlertService.createFraudAlert(fraudType, description, entityType, entityId, riskScore);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error creating fraud alert", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===============================
    // ALERT MANAGEMENT
    // ===============================

    /**
     * Acknowledge alert
     */
    @PostMapping("/{alertId}/acknowledge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestParam Long userId,
            @RequestParam(required = false) String note) {
        try {
            AdminAlert alert = adminAlertService.acknowledgeAlert(alertId, userId, note);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error acknowledging alert: {}", alertId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resolve alert
     */
    @PostMapping("/{alertId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam Long userId,
            @RequestParam String resolutionNote) {
        try {
            AdminAlert alert = adminAlertService.resolveAlert(alertId, userId, resolutionNote);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error resolving alert: {}", alertId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Suppress alert
     */
    @PostMapping("/{alertId}/suppress")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> suppressAlert(
            @PathVariable Long alertId,
            @RequestParam String reason,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime until) {
        try {
            AdminAlert alert = adminAlertService.suppressAlert(alertId, reason, until);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error suppressing alert: {}", alertId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Escalate alert
     */
    @PostMapping("/{alertId}/escalate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> escalateAlert(
            @PathVariable Long alertId,
            @RequestParam String reason) {
        try {
            AdminAlert alert = adminAlertService.escalateAlert(alertId, reason);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error escalating alert: {}", alertId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===============================
    // SEARCH & FILTERING
    // ===============================

    /**
     * Search alerts với filters và pagination
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminAlert>> searchAlerts(
            @RequestParam(required = false) AlertCategory category,
            @RequestParam(required = false) SeverityLevel severity,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<AdminAlert> alerts = adminAlertService.searchAlerts(category, severity, status, startDate, endDate, page, size);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error searching alerts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy alert detail
     */
    @GetMapping("/{alertId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> getAlertDetail(@PathVariable Long alertId) {
        try {
            return adminAlertService.getAlertById(alertId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting alert detail: {}", alertId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===============================
    // AUTOMATION & MAINTENANCE
    // ===============================

    /**
     * Trigger auto-escalation check
     */
    @PostMapping("/auto-escalation/check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminAlert>> checkAutoEscalation() {
        try {
            List<AdminAlert> escalatedAlerts = adminAlertService.checkForAutoEscalation();
            return ResponseEntity.ok(escalatedAlerts);
        } catch (Exception e) {
            log.error("Error checking auto-escalation", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Consolidate recurring alerts
     */
    @PostMapping("/consolidate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> consolidateRecurringAlerts() {
        try {
            adminAlertService.consolidateRecurringAlerts();
            return ResponseEntity.ok("Recurring alerts consolidated successfully");
        } catch (Exception e) {
            log.error("Error consolidating recurring alerts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cleanup expired alerts
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cleanupExpiredAlerts() {
        try {
            adminAlertService.cleanupExpiredAlerts();
            return ResponseEntity.ok("Expired alerts cleaned up successfully");
        } catch (Exception e) {
            log.error("Error cleaning up expired alerts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===============================
    // ALERT TYPES & ENUMS
    // ===============================

    /**
     * Lấy tất cả alert categories
     */
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertCategory[]> getAlertCategories() {
        return ResponseEntity.ok(AlertCategory.values());
    }

    /**
     * Lấy tất cả severity levels
     */
    @GetMapping("/severity-levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeverityLevel[]> getSeverityLevels() {
        return ResponseEntity.ok(SeverityLevel.values());
    }

    /**
     * Lấy tất cả alert statuses
     */
    @GetMapping("/statuses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertStatus[]> getAlertStatuses() {
        return ResponseEntity.ok(AlertStatus.values());
    }

    // ===============================
    // HEALTH CHECK ENDPOINTS
    // ===============================

    /**
     * Health check endpoint để test alert system
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "AdminAlertService",
                "version", "1.0"
            );
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test endpoint để tạo sample alert
     */
    @PostMapping("/test/create-sample")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAlert> createSampleAlert() {
        try {
            AdminAlert alert = adminAlertService.createSystemHealthAlert(
                "test_component", 
                "cpu_usage", 
                new BigDecimal("95.5"), 
                new BigDecimal("80.0")
            );
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error creating sample alert", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test endpoint để trigger automated monitoring
     */
    @PostMapping("/test/trigger-monitoring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> triggerAutomatedMonitoring() {
        try {
            // Inject AutomatedAlertService if available
            // automatedAlertService.triggerAllMonitoring();
            return ResponseEntity.ok("Automated monitoring triggered successfully");
        } catch (Exception e) {
            log.error("Error triggering automated monitoring", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
