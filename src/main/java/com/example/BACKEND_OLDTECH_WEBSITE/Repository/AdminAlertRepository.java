package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminAlert;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminAlert.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Admin Alert Repository - Quản lý cảnh báo hệ thống
 * Hỗ trợ theo dõi sức khỏe hệ thống 24/7
 */
@Repository
public interface AdminAlertRepository extends JpaRepository<AdminAlert, Long> {    // ===============================
    // BASIC QUERIES
    // ===============================

    /**
     * Find alert by alert code
     */
    Optional<AdminAlert> findByAlertCode(String alertCode);

    // ===============================
    // ACTIVE ALERTS
    // ===============================

    /**
     * Lấy tất cả alert đang active - Updated for simplified model
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertStatus = 'ACTIVE' ORDER BY a.severityLevel DESC, a.createdAt DESC")
    List<AdminAlert> findActiveAlerts();

    /**
     * Lấy alert active theo category - Updated for simplified model
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertStatus = 'ACTIVE' AND a.alertCategory = :category ORDER BY a.severityLevel DESC, a.createdAt DESC")
    List<AdminAlert> findActiveAlertsByCategory(@Param("category") AlertCategory category);

    /**
     * Lấy alert active theo severity - Updated for simplified model
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertStatus = 'ACTIVE' AND a.severityLevel = :severity ORDER BY a.createdAt DESC")
    List<AdminAlert> findActiveAlertsBySeverity(@Param("severity") SeverityLevel severity);

    /**
     * Lấy alert critical cần xử lý gấp - Updated for simplified model
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertStatus = 'ACTIVE' AND (a.severityLevel = 'CRITICAL' OR a.priorityLevel = 'URGENT') ORDER BY a.createdAt DESC")
    List<AdminAlert> findCriticalAlerts();

    // ===============================
    // DASHBOARD QUERIES
    // ===============================

    /**
     * Đếm alert theo status
     */
    @Query("SELECT a.alertStatus, COUNT(a) FROM AdminAlert a WHERE a.createdAt >= :startDate GROUP BY a.alertStatus")
    List<Object[]> countAlertsByStatus(@Param("startDate") LocalDateTime startDate);

    /**
     * Đếm alert theo severity
     */
    @Query("SELECT a.severityLevel, COUNT(a) FROM AdminAlert a WHERE a.createdAt >= :startDate AND a.alertStatus = 'ACTIVE' GROUP BY a.severityLevel")
    List<Object[]> countActiveBySeverity(@Param("startDate") LocalDateTime startDate);

    /**
     * Đếm alert theo category
     */
    @Query("SELECT a.alertCategory, COUNT(a) FROM AdminAlert a WHERE a.createdAt >= :startDate GROUP BY a.alertCategory")
    List<Object[]> countAlertsByCategory(@Param("startDate") LocalDateTime startDate);

    /**
     * Lấy top metrics gây ra alert nhiều nhất
     */
    @Query("SELECT a.metricName, COUNT(a) as alertCount FROM AdminAlert a WHERE a.createdAt >= :startDate AND a.metricName IS NOT NULL GROUP BY a.metricName ORDER BY alertCount DESC")
    List<Object[]> getTopAlertMetrics(@Param("startDate") LocalDateTime startDate, Pageable pageable);    // ===============================
    // MONITORING QUERIES
    // ===============================

    /**
     * Get alerts needing escalation - Updated for simplified model
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertStatus = 'ACTIVE' AND a.severityLevel = 'CRITICAL' AND a.createdAt <= :thresholdTime")
    List<AdminAlert> findAlertsNeedingEscalation(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * Lấy alert chưa được acknowledge
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertStatus = 'ACTIVE' AND a.acknowledgedAt IS NULL ORDER BY a.severityLevel DESC, a.createdAt DESC")
    List<AdminAlert> findUnacknowledgedAlerts();

    /**
     * Lấy alert lặp lại (recurring)
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.isRecurring = true AND a.alertStatus = 'ACTIVE' ORDER BY a.occurrenceCount DESC, a.createdAt DESC")
    List<AdminAlert> findRecurringAlerts();

    /**
     * Tìm alert tương tự trong khoảng thời gian
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertType = :alertType AND a.metricName = :metricName AND a.createdAt >= :since AND a.alertId != :excludeId ORDER BY a.createdAt DESC")
    List<AdminAlert> findSimilarAlerts(@Param("alertType") String alertType, 
                                      @Param("metricName") String metricName, 
                                      @Param("since") LocalDateTime since,
                                      @Param("excludeId") Long excludeId);

    // ===============================
    // BUSINESS LOGIC QUERIES
    // ===============================

    /**
     * Lấy alert liên quan đến entity cụ thể
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.relatedEntityType = :entityType AND a.relatedEntityId = :entityId ORDER BY a.createdAt DESC")
    List<AdminAlert> findByRelatedEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    /**
     * Lấy fraud alerts đang active
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertCategory = 'FRAUD_DETECTION' AND a.alertStatus = 'ACTIVE' ORDER BY a.severityLevel DESC, a.createdAt DESC")
    List<AdminAlert> findActiveFraudAlerts();

    /**
     * Lấy system health alerts
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertCategory = 'SYSTEM_HEALTH' AND a.alertStatus = 'ACTIVE' ORDER BY a.severityLevel DESC, a.createdAt DESC")
    List<AdminAlert> findActiveSystemHealthAlerts();

    /**
     * Lấy performance degradation alerts
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertCategory = 'PERFORMANCE' AND a.alertStatus = 'ACTIVE' ORDER BY a.severityLevel DESC, a.createdAt DESC")
    List<AdminAlert> findActivePerformanceAlerts();

    /**
     * Lấy business KPI alerts
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.alertCategory = 'BUSINESS_KPI' AND a.alertStatus = 'ACTIVE' ORDER BY a.severityLevel DESC, a.createdAt DESC")
    List<AdminAlert> findActiveBusinessKpiAlerts();

    // ===============================
    // PAGINATION & FILTERING
    // ===============================

    /**
     * Lấy alert với filter và pagination
     */
    @Query("SELECT a FROM AdminAlert a WHERE " +
           "(:category IS NULL OR a.alertCategory = :category) AND " +
           "(:severity IS NULL OR a.severityLevel = :severity) AND " +
           "(:status IS NULL OR a.alertStatus = :status) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.severityLevel DESC, a.createdAt DESC")
    Page<AdminAlert> findWithFilters(@Param("category") AlertCategory category,
                                   @Param("severity") SeverityLevel severity,
                                   @Param("status") AlertStatus status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    /**
     * Search alert by title or description
     */
    @Query("SELECT a FROM AdminAlert a WHERE " +
           "(LOWER(a.alertTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.alertDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY a.severityLevel DESC, a.createdAt DESC")
    Page<AdminAlert> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // ===============================
    // ALERT SUMMARY FOR DASHBOARD
    // ===============================

    /**
     * Alert summary cho dashboard
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN a.alertStatus = 'ACTIVE' THEN 1 END) as activeCount, " +
           "COUNT(CASE WHEN a.severityLevel = 'CRITICAL' AND a.alertStatus = 'ACTIVE' THEN 1 END) as criticalCount, " +
           "COUNT(CASE WHEN a.alertStatus = 'RESOLVED' AND a.resolvedAt >= :todayStart THEN 1 END) as resolvedTodayCount, " +
           "AVG(CASE WHEN a.alertStatus = 'RESOLVED' AND a.resolutionTimeMinutes IS NOT NULL THEN a.resolutionTimeMinutes END) as avgResolutionTime " +
           "FROM AdminAlert a WHERE a.createdAt >= :startDate")
    List<Object[]> getAlertSummary(@Param("startDate") LocalDateTime startDate, @Param("todayStart") LocalDateTime todayStart);

    /**
     * Tìm alert đã hết hạn
     */
    @Query("SELECT a FROM AdminAlert a WHERE a.expiresAt IS NOT NULL AND a.expiresAt <= :currentTime")
    List<AdminAlert> findExpiredAlerts(@Param("currentTime") LocalDateTime currentTime);

}
