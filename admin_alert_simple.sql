-- ===============================
-- ADMIN ALERT TABLE - SIMPLE VERSION
-- ===============================
-- Phiên bản đơn giản nhất
-- ===============================

DROP TABLE IF EXISTS `admin_alert`;

CREATE TABLE `admin_alert` (
    -- ===============================
    -- BASIC FIELDS
    -- ===============================
    `alert_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `alert_code` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã cảnh báo',
    
    -- ===============================
    -- CORE INFO
    -- ===============================
    `alert_category` ENUM(
        'SYSTEM_HEALTH',
        'SECURITY', 
        'PERFORMANCE',
        'BUSINESS_KPI',
        'USER_ACTIVITY',
        'FRAUD_DETECTION'
    ) NOT NULL COMMENT 'Danh mục cảnh báo',
    
    `alert_type` VARCHAR(100) NOT NULL COMMENT 'Loại cảnh báo',
    `alert_title` VARCHAR(255) NOT NULL COMMENT 'Tiêu đề',
    `alert_description` TEXT COMMENT 'Mô tả',
    
    -- ===============================
    -- STATUS
    -- ===============================
    `severity_level` ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO') 
        NOT NULL DEFAULT 'MEDIUM' COMMENT 'Mức độ nghiêm trọng',
    
    `priority_level` ENUM('URGENT', 'HIGH', 'NORMAL', 'LOW') 
        NOT NULL DEFAULT 'NORMAL' COMMENT 'Mức độ ưu tiên',
    
    `alert_status` ENUM('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED', 'DISMISSED', 'ESCALATED') 
        NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái',
    
    `is_auto_generated` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Tự động',
    
    -- ===============================
    -- METRICS (OPTIONAL)
    -- ===============================
    `metric_name` VARCHAR(100) COMMENT 'Tên metric',
    `current_value` DECIMAL(20,4) COMMENT 'Giá trị hiện tại',
    `threshold_value` DECIMAL(20,4) COMMENT 'Ngưỡng',
    `threshold_type` ENUM('ABOVE', 'BELOW', 'EQUAL', 'CHANGE_RATE') COMMENT 'Kiểu so sánh',
    
    -- ===============================
    -- BASIC CONTEXT
    -- ===============================
    `affected_component` VARCHAR(100) COMMENT 'Thành phần bị ảnh hưởng',
    `impact_level` ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW') COMMENT 'Mức độ tác động',
    `business_impact` TEXT COMMENT 'Tác động business',
    
    -- ===============================
    -- RELATED DATA
    -- ===============================
    `related_entity_type` VARCHAR(50) COMMENT 'Loại đối tượng liên quan',
    `related_entity_id` BIGINT UNSIGNED COMMENT 'ID đối tượng liên quan',
    
    -- ===============================
    -- ACTIONS
    -- ===============================
    `suggested_action` TEXT COMMENT 'Hành động đề xuất',
    `manual_action_required` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Cần can thiệp thủ công',
    
    -- ===============================
    -- TRACKING
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
    -- NOTIFICATION
    -- ===============================
    `notification_sent` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Đã gửi thông báo',
    
    -- ===============================
    -- RECURRENCE
    -- ===============================
    `is_recurring` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Alert lặp lại',
    `occurrence_count` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Số lần xuất hiện',
    
    -- ===============================
    -- METADATA
    -- ===============================
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
COMMENT='Admin Alert System - Simple Version';

-- ===============================
-- SAMPLE DATA
-- ===============================

INSERT INTO `admin_alert` (
    `alert_code`, `alert_category`, `alert_type`, `alert_title`, `alert_description`,
    `severity_level`, `priority_level`, `affected_component`, `impact_level`,
    `suggested_action`, `source_system`
) VALUES 
('SYS_001', 'SYSTEM_HEALTH', 'CPU_HIGH', 'High CPU Usage', 'CPU usage exceeded 90%', 
 'HIGH', 'HIGH', 'Application Server', 'HIGH', 
 'Check server resources', 'MONITORING'),

('SEC_001', 'SECURITY', 'LOGIN_FAIL', 'Multiple Login Failures', 'Suspicious login attempts detected',
 'CRITICAL', 'URGENT', 'Auth System', 'CRITICAL', 
 'Block suspicious IPs', 'SECURITY'),

('PERF_001', 'PERFORMANCE', 'SLOW_DB', 'Database Performance', 'Slow query detected',
 'MEDIUM', 'NORMAL', 'Database', 'MEDIUM', 
 'Optimize queries', 'DATABASE');
