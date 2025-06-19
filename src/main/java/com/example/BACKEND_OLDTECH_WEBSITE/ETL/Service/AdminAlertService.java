package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminAlert;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminAlert.*;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.AdminAlertRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Alert Service - Quản lý cảnh báo hệ thống 24/7
 * Theo dõi sức khỏe hệ thống, phát hiện sớm vấn đề tiềm ẩn
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminAlertService {

    private final AdminAlertRepository adminAlertRepository;
    private final UserRepository userRepository;
    
    // ===============================
    // ALERT CREATION & MANAGEMENT
    // ===============================

    /**
     * Tạo alert mới cho hệ thống
     */
    public AdminAlert createAlert(AlertCategory category, String type, String title, String description, 
                                SeverityLevel severity, String metricName, BigDecimal currentValue, 
                                BigDecimal thresholdValue, ThresholdType thresholdType) {
        
        AdminAlert alert = new AdminAlert();
        alert.setAlertCode(AdminAlert.generateAlertCode(category, type));
        alert.setAlertCategory(category);
        alert.setAlertType(type);
        alert.setAlertTitle(title);
        alert.setAlertDescription(description);
        alert.setSeverityLevel(severity);
        alert.setPriorityLevel(mapSeverityToPriority(severity));
        alert.setMetricName(metricName);
        alert.setCurrentValue(currentValue);
        alert.setThresholdValue(thresholdValue);
        alert.setThresholdType(thresholdType);
        alert.setSourceSystem("PLATFORM_MONITORING");
        
        // Auto-determine impact và suggested action
        determineImpactAndAction(alert);
        
        AdminAlert savedAlert = adminAlertRepository.save(alert);
        log.info("Created new alert: {} - {}", savedAlert.getAlertCode(), savedAlert.getAlertTitle());
        
        // Trigger notifications if critical
        if (savedAlert.isCritical()) {
            triggerCriticalAlertNotification(savedAlert);
        }
        
        return savedAlert;
    }

    /**
     * Tạo System Health Alert
     */
    public AdminAlert createSystemHealthAlert(String component, String metric, BigDecimal value, BigDecimal threshold) {
        String title = String.format("System Health Issue - %s", component);
        String description = String.format("Component %s metric %s value %.2f exceeds threshold %.2f", 
                                         component, metric, value, threshold);
        
        SeverityLevel severity = determineSeverityByThreshold(value, threshold);
        
        AdminAlert alert = createAlert(
            AlertCategory.SYSTEM_HEALTH, 
            "HEALTH_CHECK", 
            title, 
            description, 
            severity,
            metric, 
            value, 
            threshold, 
            ThresholdType.ABOVE
        );
        
        alert.setAffectedComponent(component);
        alert.setSuggestedAction(generateSystemHealthAction(component, metric));
        
        return adminAlertRepository.save(alert);
    }

    /**
     * Tạo Security Alert
     */
    public AdminAlert createSecurityAlert(String alertType, String description, String entityType, Long entityId) {
        AdminAlert alert = createAlert(
            AlertCategory.SECURITY,
            alertType,
            "Security Alert - " + alertType,
            description,
            SeverityLevel.HIGH,
            "security_violation",
            BigDecimal.ONE,
            BigDecimal.ZERO,
            ThresholdType.ABOVE
        );
        
        alert.setRelatedEntityType(entityType);
        alert.setRelatedEntityId(entityId);
        alert.setManualActionRequired(true);
        alert.setSuggestedAction("Investigate security incident immediately");
        
        return adminAlertRepository.save(alert);
    }

    /**
     * Tạo Performance Alert
     */
    public AdminAlert createPerformanceAlert(String metric, BigDecimal currentValue, BigDecimal threshold) {
        String title = String.format("Performance Degradation - %s", metric);
        String description = String.format("Performance metric %s (%.2f) has degraded below acceptable threshold (%.2f)", 
                                         metric, currentValue, threshold);
        
        AdminAlert alert = createAlert(
            AlertCategory.PERFORMANCE,
            "PERFORMANCE_DEGRADATION",
            title,
            description,
            SeverityLevel.MEDIUM,
            metric,
            currentValue,
            threshold,
            ThresholdType.BELOW
        );
        
        alert.setSuggestedAction(generatePerformanceAction(metric));
        
        return adminAlertRepository.save(alert);
    }

    /**
     * Tạo Business KPI Alert
     */
    public AdminAlert createBusinessKpiAlert(String kpiName, BigDecimal currentValue, BigDecimal expectedValue, String period) {
        String title = String.format("Business KPI Alert - %s", kpiName);
        String description = String.format("KPI %s for period %s (%.2f) is significantly different from expected (%.2f)", 
                                         kpiName, period, currentValue, expectedValue);
        
        SeverityLevel severity = determineKpiSeverity(currentValue, expectedValue);
        
        AdminAlert alert = createAlert(
            AlertCategory.BUSINESS_KPI,
            "KPI_DEVIATION",
            title,
            description,
            severity,
            kpiName,
            currentValue,
            expectedValue,
            ThresholdType.CHANGE_RATE
        );
        
        alert.setBusinessImpact(generateBusinessImpact(kpiName, currentValue, expectedValue));
        alert.setSuggestedAction(generateKpiAction(kpiName));
        
        return adminAlertRepository.save(alert);
    }

    /**
     * Tạo Fraud Detection Alert
     */
    public AdminAlert createFraudAlert(String fraudType, String description, String entityType, Long entityId, BigDecimal riskScore) {
        AdminAlert alert = createAlert(
            AlertCategory.FRAUD_DETECTION,
            fraudType,
            "Fraud Detection - " + fraudType,
            description,
            determineFraudSeverity(riskScore),
            "fraud_risk_score",
            riskScore,
            new BigDecimal("70.0"), // Threshold for fraud
            ThresholdType.ABOVE
        );
        
        alert.setRelatedEntityType(entityType);
        alert.setRelatedEntityId(entityId);
        alert.setManualActionRequired(true);
        alert.setSuggestedAction("Review transaction for potential fraud");
        
        return adminAlertRepository.save(alert);
    }


    public AdminAlert acknowledgeAlert(Long alertId, Long userId, String note) {
        AdminAlert alert = adminAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        
        User user = userRepository.findById(userId.intValue())
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        alert.acknowledge(user, note);
        AdminAlert savedAlert = adminAlertRepository.save(alert);
        
        log.info("Alert {} acknowledged by user {}", alertId, userId);
        return savedAlert;
    }    /**
     * Resolve alert
     */
    public AdminAlert resolveAlert(Long alertId, Long userId, String resolutionNote) {
        AdminAlert alert = adminAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        
        User user = userRepository.findById(userId.intValue())
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        alert.resolve(user, resolutionNote);
        AdminAlert savedAlert = adminAlertRepository.save(alert);
        
        log.info("Alert {} resolved by user {}", alertId, userId);
        return savedAlert;
    }    /**
     * Suppress alert - Simplified version
     */
    public AdminAlert suppressAlert(Long alertId, String reason, LocalDateTime until) {
        AdminAlert alert = adminAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        
        // Simple suppression - just change status to DISMISSED
        alert.setAlertStatus(AlertStatus.DISMISSED);
        alert.setResolutionNote("Suppressed: " + reason + " until " + until);
        
        AdminAlert savedAlert = adminAlertRepository.save(alert);
        
        log.info("Alert {} suppressed until {}", alertId, until);
        return savedAlert;
    }

    /**
     * Escalate alert
     */
    public AdminAlert escalateAlert(Long alertId, String escalationReason) {
        AdminAlert alert = adminAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        
        alert.setAlertStatus(AlertStatus.ESCALATED);
        alert.setSeverityLevel(SeverityLevel.CRITICAL);
        alert.setPriorityLevel(PriorityLevel.URGENT);
        alert.setManualActionRequired(true);
        
        AdminAlert savedAlert = adminAlertRepository.save(alert);
        
        log.warn("Alert {} escalated: {}", alertId, escalationReason);
        
        // Trigger escalation notifications
        triggerEscalationNotification(savedAlert, escalationReason);
        
        return savedAlert;
    }

    // ===============================
    // ALERT MONITORING & AUTOMATION
    // ===============================

    /**
     * Kiểm tra alerts cần escalate tự động
     */
    @Transactional(readOnly = true)
    public List<AdminAlert> checkForAutoEscalation() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30); // 30 phút
        List<AdminAlert> alertsNeedingEscalation = adminAlertRepository.findAlertsNeedingEscalation(threshold);
        
        for (AdminAlert alert : alertsNeedingEscalation) {
            if (alert.shouldEscalate()) {
                escalateAlert(alert.getAlertId(), "Auto-escalated: Critical alert unresolved for 30+ minutes");
            }
        }
        
        return alertsNeedingEscalation;
    }

    /**
     * Phát hiện alerts lặp lại và gộp chúng
     */
    public void consolidateRecurringAlerts() {
        List<AdminAlert> recurringAlerts = adminAlertRepository.findRecurringAlerts();
        
        Map<String, List<AdminAlert>> groupedAlerts = recurringAlerts.stream()
                .collect(Collectors.groupingBy(alert -> 
                    alert.getAlertType() + ":" + alert.getMetricName()));
        
        for (Map.Entry<String, List<AdminAlert>> entry : groupedAlerts.entrySet()) {
            List<AdminAlert> similarAlerts = entry.getValue();
            if (similarAlerts.size() > 1) {
                consolidateSimilarAlerts(similarAlerts);
            }
        }
    }

    /**
     * Cleanup alerts đã hết hạn
     */
    public void cleanupExpiredAlerts() {
        List<AdminAlert> expiredAlerts = adminAlertRepository.findExpiredAlerts(LocalDateTime.now());
        
        for (AdminAlert alert : expiredAlerts) {
            if (alert.getAlertStatus() == AlertStatus.ACTIVE) {
                alert.setAlertStatus(AlertStatus.DISMISSED);
                adminAlertRepository.save(alert);
                log.info("Auto-dismissed expired alert: {}", alert.getAlertCode());
            }
        }
    }

    // ===============================
    // DASHBOARD & REPORTING
    // ===============================

    /**
     * Lấy alert summary cho dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAlertDashboardSummary() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        
        List<Object[]> summary = adminAlertRepository.getAlertSummary(last24Hours, startOfDay);
        
        Map<String, Object> result = new HashMap<>();
        if (!summary.isEmpty()) {
            Object[] data = summary.get(0);
            result.put("activeCount", data[0]);
            result.put("criticalCount", data[1]);
            result.put("resolvedTodayCount", data[2]);
            result.put("avgResolutionTimeMinutes", data[3]);
        }
        
        // Thêm breakdown theo category
        List<Object[]> categoryBreakdown = adminAlertRepository.countAlertsByCategory(last24Hours);
        Map<String, Long> categoryMap = categoryBreakdown.stream()
                .collect(Collectors.toMap(
                    arr -> arr[0].toString(),
                    arr -> (Long) arr[1]
                ));
        result.put("categoryBreakdown", categoryMap);
        
        // Thêm severity breakdown
        List<Object[]> severityBreakdown = adminAlertRepository.countActiveBySeverity(last24Hours);
        Map<String, Long> severityMap = severityBreakdown.stream()
                .collect(Collectors.toMap(
                    arr -> arr[0].toString(),
                    arr -> (Long) arr[1]
                ));
        result.put("severityBreakdown", severityMap);
        
        return result;
    }

    /**
     * Lấy critical alerts cần xử lý ngay
     */
    @Transactional(readOnly = true)
    public List<AdminAlert> getCriticalAlerts() {
        return adminAlertRepository.findCriticalAlerts();
    }

    /**
     * Lấy alerts theo category
     */
    @Transactional(readOnly = true)
    public List<AdminAlert> getAlertsByCategory(AlertCategory category) {
        return adminAlertRepository.findActiveAlertsByCategory(category);
    }

    /**
     * Search alerts với filters
     */
    @Transactional(readOnly = true)
    public Page<AdminAlert> searchAlerts(AlertCategory category, SeverityLevel severity, AlertStatus status, 
                                       LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return adminAlertRepository.findWithFilters(category, severity, status, startDate, endDate, pageable);
    }

    /**
     * Lấy alert theo ID
     */
    @Transactional(readOnly = true)
    public Optional<AdminAlert> getAlertById(Long alertId) {
        return adminAlertRepository.findById(alertId);
    }

    // ===============================
    // HELPER METHODS
    // ===============================

    private PriorityLevel mapSeverityToPriority(SeverityLevel severity) {
        switch (severity) {
            case CRITICAL:
                return PriorityLevel.URGENT;
            case HIGH:
                return PriorityLevel.HIGH;
            case MEDIUM:
                return PriorityLevel.NORMAL;
            case LOW:
            case INFO:
                return PriorityLevel.LOW;
            default:
                return PriorityLevel.NORMAL;
        }
    }

    private void determineImpactAndAction(AdminAlert alert) {
        switch (alert.getAlertCategory()) {
            case SYSTEM_HEALTH:
                alert.setImpactLevel(ImpactLevel.HIGH);
                alert.setBusinessImpact("System instability may affect user experience");
                break;
            case SECURITY:
                alert.setImpactLevel(ImpactLevel.CRITICAL);
                alert.setBusinessImpact("Security breach may compromise user data and platform integrity");
                break;
            case PERFORMANCE:
                alert.setImpactLevel(ImpactLevel.MEDIUM);
                alert.setBusinessImpact("Performance degradation may affect user satisfaction");
                break;
            case BUSINESS_KPI:
                alert.setImpactLevel(ImpactLevel.HIGH);
                alert.setBusinessImpact("KPI deviation may indicate business process issues");
                break;
            case FRAUD_DETECTION:
                alert.setImpactLevel(ImpactLevel.HIGH);
                alert.setBusinessImpact("Potential fraud may cause financial loss");
                break;
            default:
                alert.setImpactLevel(ImpactLevel.MEDIUM);
                break;
        }
    }

    private SeverityLevel determineSeverityByThreshold(BigDecimal value, BigDecimal threshold) {
        if (threshold.compareTo(BigDecimal.ZERO) == 0) return SeverityLevel.MEDIUM;
        
        BigDecimal ratio = value.divide(threshold, 2, RoundingMode.HALF_UP);
        
        if (ratio.compareTo(new BigDecimal("2.0")) >= 0) {
            return SeverityLevel.CRITICAL;
        } else if (ratio.compareTo(new BigDecimal("1.5")) >= 0) {
            return SeverityLevel.HIGH;
        } else if (ratio.compareTo(new BigDecimal("1.2")) >= 0) {
            return SeverityLevel.MEDIUM;
        } else {
            return SeverityLevel.LOW;
        }
    }

    private SeverityLevel determineKpiSeverity(BigDecimal current, BigDecimal expected) {
        if (expected.compareTo(BigDecimal.ZERO) == 0) return SeverityLevel.MEDIUM;
        
        BigDecimal deviationPercent = current.subtract(expected)
                .divide(expected, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .abs();
        
        if (deviationPercent.compareTo(new BigDecimal("50")) >= 0) {
            return SeverityLevel.CRITICAL;
        } else if (deviationPercent.compareTo(new BigDecimal("25")) >= 0) {
            return SeverityLevel.HIGH;
        } else if (deviationPercent.compareTo(new BigDecimal("10")) >= 0) {
            return SeverityLevel.MEDIUM;
        } else {
            return SeverityLevel.LOW;
        }
    }

    private SeverityLevel determineFraudSeverity(BigDecimal riskScore) {
        if (riskScore.compareTo(new BigDecimal("90")) >= 0) {
            return SeverityLevel.CRITICAL;
        } else if (riskScore.compareTo(new BigDecimal("70")) >= 0) {
            return SeverityLevel.HIGH;
        } else if (riskScore.compareTo(new BigDecimal("50")) >= 0) {
            return SeverityLevel.MEDIUM;
        } else {
            return SeverityLevel.LOW;
        }
    }

    private String generateSystemHealthAction(String component, String metric) {
        return String.format("Check %s component logs and monitoring for %s metric. Consider restarting service if necessary.", component, metric);
    }

    private String generatePerformanceAction(String metric) {
        return String.format("Investigate performance bottleneck for %s. Check resource utilization and optimize if needed.", metric);
    }

    private String generateKpiAction(String kpiName) {
        return String.format("Analyze root cause for %s deviation. Review business processes and take corrective action.", kpiName);
    }

    private String generateBusinessImpact(String kpiName, BigDecimal current, BigDecimal expected) {
        BigDecimal deviation = current.subtract(expected).divide(expected, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        return String.format("KPI %s deviation of %.2f%% may impact business performance and revenue", kpiName, deviation);
    }    private void consolidateSimilarAlerts(List<AdminAlert> similarAlerts) {
        // Giữ alert đầu tiên, cập nhật occurrence count
        AdminAlert primaryAlert = similarAlerts.get(0);
        primaryAlert.setOccurrenceCount(similarAlerts.size());
        primaryAlert.setIsRecurring(true);
        // Note: recurrence_pattern field removed in simplified version
        
        // Dismiss các alerts khác
        for (int i = 1; i < similarAlerts.size(); i++) {
            AdminAlert duplicateAlert = similarAlerts.get(i);
            duplicateAlert.setAlertStatus(AlertStatus.DISMISSED);
            duplicateAlert.setResolutionNote("Consolidated with alert: " + primaryAlert.getAlertCode());
            adminAlertRepository.save(duplicateAlert);
        }
        
        adminAlertRepository.save(primaryAlert);
        log.info("Consolidated {} similar alerts into {}", similarAlerts.size(), primaryAlert.getAlertCode());
    }

    private void triggerCriticalAlertNotification(AdminAlert alert) {
        // TODO: Implement notification logic (email, SMS, Slack, etc.)
        log.warn("CRITICAL ALERT: {} - {}", alert.getAlertCode(), alert.getAlertTitle());
    }

    private void triggerEscalationNotification(AdminAlert alert, String reason) {
        // TODO: Implement escalation notification logic
        log.error("ALERT ESCALATED: {} - {} - Reason: {}", alert.getAlertCode(), alert.getAlertTitle(), reason);
    }
}
