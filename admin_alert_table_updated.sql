-- ===============================
-- ADMIN ALERT TABLE - REAL-TIME MONITORING 🔍
-- ===============================
-- Hệ thống cảnh báo admin đơn giản và hiệu quả
-- Updated: June 20, 2025
-- ===============================

DROP TABLE IF EXISTS `admin_alert`;

CREATE TABLE `admin_alert` (
    -- ===============================
    -- BASIC IDENTIFICATION
    -- ===============================
    `alert_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `alert_code` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã cảnh báo',
    
    -- ===============================
    -- ALERT CORE INFO
    -- ===============================
    `alert_category` ENUM(
        'SYSTEM_HEALTH',     -- Sức khỏe hệ thống
        'SECURITY',          -- Bảo mật
        'PERFORMANCE',       -- Hiệu suất
        'BUSINESS_KPI',      -- KPI kinh doanh
        'USER_ACTIVITY',     -- Hoạt động người dùng
        'FRAUD_DETECTION'    -- Phát hiện gian lận
    ) NOT NULL COMMENT 'Danh mục cảnh báo',
    
    `alert_type` VARCHAR(100) NOT NULL COMMENT 'Loại cảnh báo cụ thể',
    `alert_title` VARCHAR(255) NOT NULL COMMENT 'Tiêu đề cảnh báo',
    `alert_description` TEXT COMMENT 'Mô tả chi tiết',
    
    -- ===============================
    -- SEVERITY & PRIORITY
    -- ===============================
    `severity_level` ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO') 
        NOT NULL DEFAULT 'MEDIUM' COMMENT 'Mức độ nghiêm trọng',
    
    `priority_level` ENUM('URGENT', 'HIGH', 'NORMAL', 'LOW') 
        NOT NULL DEFAULT 'NORMAL' COMMENT 'Mức độ ưu tiên',
    
    `alert_status` ENUM('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED', 'DISMISSED', 'ESCALATED') 
        NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái xử lý',
    
    `is_auto_generated` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Tự động hay thủ công',
    
    -- ===============================
    -- METRICS & MONITORING
    -- ===============================
    `metric_name` VARCHAR(100) COMMENT 'Tên metric',
    `current_value` DECIMAL(20,4) COMMENT 'Giá trị hiện tại',
    `threshold_value` DECIMAL(20,4) COMMENT 'Ngưỡng cảnh báo',
    `threshold_type` ENUM('ABOVE', 'BELOW', 'EQUAL', 'CHANGE_RATE') COMMENT 'Kiểu so sánh',
    
    -- ===============================
    -- SYSTEM CONTEXT
    -- ===============================
    `affected_component` VARCHAR(100) COMMENT 'Thành phần bị ảnh hưởng',
    `impact_level` ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW') COMMENT 'Mức độ tác động',
    `business_impact` TEXT COMMENT 'Tác động business',
    `technical_details` JSON COMMENT 'Chi tiết kỹ thuật dạng JSON',
    
    -- ===============================
    -- RELATED DATA
    -- ===============================
    `related_entity_type` VARCHAR(50) COMMENT 'Loại đối tượng liên quan',
    `related_entity_id` BIGINT UNSIGNED COMMENT 'ID đối tượng liên quan',
    `correlation_id` VARCHAR(100) COMMENT 'ID tương quan',
    
    -- ===============================
    -- ACTIONS & RESPONSES
    -- ===============================
    `suggested_action` TEXT COMMENT 'Hành động đề xuất',
    `auto_action_taken` TEXT COMMENT 'Hành động tự động đã thực hiện',
    `manual_action_required` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Cần can thiệp thủ công',
    `escalation_rules` JSON COMMENT 'Quy tắc leo thang',
    
    -- ===============================
    -- ALERT STATUS & TRACKING
    -- ===============================
    `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Đã đọc',
    `is_resolved` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Đã giải quyết',
    
    -- ===============================
    -- ACKNOWLEDGMENT & RESOLUTION
    -- ===============================
    `acknowledged_by` INT UNSIGNED COMMENT 'User ID người xác nhận',
    `acknowledged_at` DATETIME COMMENT 'Thời gian xác nhận',
    `acknowledged_note` TEXT COMMENT 'Ghi chú xác nhận',
    
    `resolved_by` INT UNSIGNED COMMENT 'User ID người giải quyết',
    `resolved_at` DATETIME COMMENT 'Thời gian giải quyết',
    `resolution_note` TEXT COMMENT 'Ghi chú giải quyết',
    `resolution_time_minutes` INT UNSIGNED COMMENT 'Thời gian giải quyết (phút)',
    
    -- ===============================
    -- NOTIFICATION & COMMUNICATION
    -- ===============================
    `notification_sent` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Đã gửi thông báo',
    `notification_channels` JSON COMMENT 'Kênh thông báo',
    `notification_recipients` JSON COMMENT 'Người nhận thông báo',
    
    -- ===============================
    -- RECURRENCE & PATTERN
    -- ===============================
    `is_recurring` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Alert lặp lại',
    `recurrence_pattern` VARCHAR(100) COMMENT 'Mẫu lặp lại',
    `first_occurrence_at` DATETIME COMMENT 'Lần xuất hiện đầu tiên',
    `last_occurrence_at` DATETIME COMMENT 'Lần xuất hiện cuối',
    `occurrence_count` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Số lần xuất hiện',
    
    -- ===============================
    -- SUPPRESSION
    -- ===============================
    `is_suppressed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Bị tạm ngưng',
    `suppressed_until` DATETIME COMMENT 'Tạm ngưng đến',
    `suppression_reason` TEXT COMMENT 'Lý do tạm ngưng',
    
    -- ===============================
    -- SLA & PERFORMANCE
    -- ===============================
    `sla_breach` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Vi phạm SLA',
    `response_time_minutes` INT UNSIGNED COMMENT 'Thời gian phản hồi (phút)',
    `mttr_minutes` INT UNSIGNED COMMENT 'Thời gian trung bình để giải quyết',
    
    -- ===============================
    -- METADATA
    -- ===============================
    `environment` VARCHAR(20) DEFAULT 'PRODUCTION' COMMENT 'Môi trường',
    `source_system` VARCHAR(100) COMMENT 'Hệ thống nguồn',
    `alert_version` VARCHAR(10) DEFAULT '1.0' COMMENT 'Phiên bản alert',
    
    -- ===============================
    -- TIMESTAMPS
    -- ===============================
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',
    `expires_at` DATETIME COMMENT 'Thời gian hết hạn',
    
    -- ===============================
    -- INDEXES
    -- ===============================
    INDEX `idx_alert_status` (`alert_status`),
    INDEX `idx_severity_level` (`severity_level`),
    INDEX `idx_alert_category` (`alert_category`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_acknowledged_by` (`acknowledged_by`),
    INDEX `idx_resolved_by` (`resolved_by`),
    INDEX `idx_related_entity` (`related_entity_type`, `related_entity_id`),
    INDEX `idx_alert_code` (`alert_code`),
    INDEX `idx_is_suppressed` (`is_suppressed`),
    INDEX `idx_expires_at` (`expires_at`),
    
    -- ===============================
    -- FOREIGN KEYS
    -- ===============================
    CONSTRAINT `fk_admin_alert_acknowledged_by` 
        FOREIGN KEY (`acknowledged_by`) REFERENCES `user`(`user_id`) 
        ON DELETE SET NULL ON UPDATE CASCADE,
    
    CONSTRAINT `fk_admin_alert_resolved_by` 
        FOREIGN KEY (`resolved_by`) REFERENCES `user`(`user_id`) 
        ON DELETE SET NULL ON UPDATE CASCADE
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Hệ thống cảnh báo admin - Real-time monitoring v2.0';

-- ===============================
-- SAMPLE DATA (OPTIONAL)
-- ===============================

-- Insert sample alerts for testing
INSERT INTO `admin_alert` (
    `alert_code`, `alert_category`, `alert_type`, `alert_title`, `alert_description`,
    `severity_level`, `priority_level`, `metric_name`, `current_value`, `threshold_value`,
    `threshold_type`, `affected_component`, `impact_level`, `business_impact`,
    `suggested_action`, `source_system`
) VALUES 
('SYS_CPU_001', 'SYSTEM_HEALTH', 'CPU_HIGH', 'High CPU Usage Alert', 'CPU usage exceeded 90%', 
 'HIGH', 'HIGH', 'cpu_usage_percent', 95.5, 90.0, 'ABOVE', 'Application Server', 
 'HIGH', 'High CPU may affect application performance', 
 'Check for memory leaks or increase server resources', 'PLATFORM_MONITORING'),

('SEC_LOGIN_001', 'SECURITY', 'SUSPICIOUS_LOGIN', 'Suspicious Login Attempt', 'Multiple failed login attempts detected',
 'CRITICAL', 'URGENT', 'failed_login_count', 10, 5, 'ABOVE', 'Authentication System',
 'CRITICAL', 'Potential security breach attempt',
 'Review logs and block suspicious IPs if necessary', 'SECURITY_MONITORING'),

('PERF_DB_001', 'PERFORMANCE', 'SLOW_QUERY', 'Database Slow Query Alert', 'Query execution time exceeded threshold',
 'MEDIUM', 'NORMAL', 'query_execution_time', 5.2, 3.0, 'ABOVE', 'Database Server',
 'MEDIUM', 'Slow queries may affect user experience',
 'Review and optimize query performance', 'DATABASE_MONITORING'),

('BIZ_SALES_001', 'BUSINESS_KPI', 'SALES_DROP', 'Sales Revenue Drop Alert', 'Daily sales revenue below expected threshold',
 'HIGH', 'HIGH', 'daily_sales_revenue', 8500.00, 10000.00, 'BELOW', 'E-commerce Platform',
 'HIGH', 'Revenue loss may indicate business issues',
 'Investigate marketing campaigns and product availability', 'BUSINESS_INTELLIGENCE'),

('FRAUD_TXN_001', 'FRAUD_DETECTION', 'SUSPICIOUS_TRANSACTION', 'Suspicious Transaction Detected', 'Transaction pattern indicates potential fraud',
 'CRITICAL', 'URGENT', 'fraud_risk_score', 85.0, 70.0, 'ABOVE', 'Payment System',
 'CRITICAL', 'Potential financial fraud detected',
 'Review transaction details and suspend account if necessary', 'FRAUD_MONITORING');

-- ===============================
-- UTILITY PROCEDURES
-- ===============================

DELIMITER ;;

-- Procedure to cleanup old resolved alerts
CREATE PROCEDURE CleanupOldAlerts()
BEGIN
    DELETE FROM admin_alert 
    WHERE alert_status = 'RESOLVED' 
    AND created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
    
    SELECT ROW_COUNT() as deleted_alerts;
END;;

-- Procedure to get alert statistics
CREATE PROCEDURE GetAlertStatistics(IN days_back INT)
BEGIN
    SELECT 
        alert_category,
        severity_level,
        COUNT(*) as alert_count,
        AVG(response_time_minutes) as avg_response_time,
        AVG(resolution_time_minutes) as avg_resolution_time,
        SUM(CASE WHEN alert_status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_count,
        SUM(CASE WHEN alert_status = 'ACTIVE' THEN 1 ELSE 0 END) as active_count
    FROM admin_alert 
    WHERE created_at >= DATE_SUB(NOW(), INTERVAL days_back DAY)
    GROUP BY alert_category, severity_level
    ORDER BY alert_count DESC;
END;;

-- Procedure to escalate old unresolved critical alerts
CREATE PROCEDURE EscalateOldCriticalAlerts()
BEGIN
    UPDATE admin_alert 
    SET alert_status = 'ESCALATED',
        priority_level = 'URGENT',
        updated_at = NOW()
    WHERE alert_status = 'ACTIVE'
    AND severity_level = 'CRITICAL'
    AND created_at <= DATE_SUB(NOW(), INTERVAL 30 MINUTE)
    AND acknowledged_at IS NULL;
    
    SELECT ROW_COUNT() as escalated_alerts;
END;;

-- Function to calculate alert age in minutes
CREATE FUNCTION GetAlertAgeMinutes(alert_created_at DATETIME)
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    RETURN TIMESTAMPDIFF(MINUTE, alert_created_at, NOW());
END;;

DELIMITER ;

-- ===============================
-- VIEWS FOR REPORTING
-- ===============================

-- View for active alerts dashboard
CREATE VIEW v_active_alerts AS
SELECT 
    alert_id,
    alert_code,
    alert_category,
    alert_type,
    alert_title,
    severity_level,
    priority_level,
    alert_status,
    affected_component,
    created_at,
    GetAlertAgeMinutes(created_at) as age_minutes,
    CASE 
        WHEN severity_level = 'CRITICAL' THEN '#DC2626'
        WHEN severity_level = 'HIGH' THEN '#EA580C'
        WHEN severity_level = 'MEDIUM' THEN '#D97706'
        WHEN severity_level = 'LOW' THEN '#059669'
        ELSE '#0284C7'
    END as display_color
FROM admin_alert 
WHERE alert_status IN ('ACTIVE', 'ACKNOWLEDGED')
AND (is_suppressed = 0 OR suppressed_until < NOW())
ORDER BY 
    FIELD(severity_level, 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'),
    created_at DESC;

-- View for alert summary statistics
CREATE VIEW v_alert_summary AS
SELECT 
    DATE(created_at) as alert_date,
    alert_category,
    severity_level,
    COUNT(*) as total_alerts,
    SUM(CASE WHEN alert_status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_alerts,
    SUM(CASE WHEN alert_status = 'ACTIVE' THEN 1 ELSE 0 END) as active_alerts,
    AVG(resolution_time_minutes) as avg_resolution_time,
    AVG(response_time_minutes) as avg_response_time
FROM admin_alert 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(created_at), alert_category, severity_level
ORDER BY alert_date DESC, alert_category, severity_level;

-- ===============================
-- TRIGGERS FOR AUTOMATION
-- ===============================

DELIMITER ;;

-- Trigger to automatically set timestamps and calculate response time
CREATE TRIGGER tr_admin_alert_before_update
BEFORE UPDATE ON admin_alert
FOR EACH ROW
BEGIN
    -- Update the updated_at timestamp
    SET NEW.updated_at = NOW();
    
    -- Set response time when first acknowledged
    IF OLD.acknowledged_at IS NULL AND NEW.acknowledged_at IS NOT NULL THEN
        SET NEW.response_time_minutes = TIMESTAMPDIFF(MINUTE, NEW.created_at, NEW.acknowledged_at);
    END IF;
    
    -- Set resolution time and mark as resolved when resolved
    IF OLD.resolved_at IS NULL AND NEW.resolved_at IS NOT NULL THEN
        SET NEW.resolution_time_minutes = TIMESTAMPDIFF(MINUTE, NEW.created_at, NEW.resolved_at);
        SET NEW.is_resolved = 1;
        SET NEW.alert_status = 'RESOLVED';
    END IF;
    
    -- Auto-update occurrence count and last occurrence
    IF NEW.is_recurring = 1 THEN
        SET NEW.last_occurrence_at = NOW();
        SET NEW.occurrence_count = NEW.occurrence_count + 1;
    END IF;
END;;

-- Trigger to set initial timestamps on insert
CREATE TRIGGER tr_admin_alert_before_insert
BEFORE INSERT ON admin_alert
FOR EACH ROW
BEGIN
    SET NEW.created_at = NOW();
    SET NEW.updated_at = NOW();
    
    IF NEW.first_occurrence_at IS NULL THEN
        SET NEW.first_occurrence_at = NOW();
    END IF;
    
    SET NEW.last_occurrence_at = NOW();
    
    -- Generate alert code if not provided
    IF NEW.alert_code IS NULL OR NEW.alert_code = '' THEN
        SET NEW.alert_code = CONCAT(
            SUBSTRING(NEW.alert_category, 1, 3), 
            '_', 
            UPPER(SUBSTRING(NEW.alert_type, 1, 3)),
            '_',
            LPAD(CONNECTION_ID(), 6, '0')
        );
    END IF;
END;;

DELIMITER ;

-- ===============================
-- INDEXES FOR PERFORMANCE
-- ===============================

-- Additional composite indexes for common queries
CREATE INDEX `idx_status_severity_created` ON `admin_alert` (`alert_status`, `severity_level`, `created_at`);
CREATE INDEX `idx_category_status_created` ON `admin_alert` (`alert_category`, `alert_status`, `created_at`);
CREATE INDEX `idx_suppressed_until` ON `admin_alert` (`is_suppressed`, `suppressed_until`);
CREATE INDEX `idx_recurring_pattern` ON `admin_alert` (`is_recurring`, `recurrence_pattern`);

-- ===============================
-- GRANTS (OPTIONAL)
-- ===============================

-- Grant permissions to application user (adjust as needed)
-- GRANT SELECT, INSERT, UPDATE ON admin_alert TO 'app_user'@'localhost';
-- GRANT EXECUTE ON PROCEDURE CleanupOldAlerts TO 'app_user'@'localhost';
-- GRANT EXECUTE ON PROCEDURE GetAlertStatistics TO 'app_user'@'localhost';
-- GRANT EXECUTE ON PROCEDURE EscalateOldCriticalAlerts TO 'app_user'@'localhost';

-- ===============================
-- END OF SCRIPT
-- ===============================
