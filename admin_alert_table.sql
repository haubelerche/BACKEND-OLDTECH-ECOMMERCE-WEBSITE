-- ===============================
-- ADMIN ALERT TABLE - REAL-TIME MONITORING 🔍
-- ===============================
-- Hệ thống cảnh báo admin đơn giản và hiệu quả
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
        'FRAUD_DETECTION'    -- Phát hiện gian lận
    ) NOT NULL COMMENT 'Danh mục cảnh báo',
    
    `alert_type` VARCHAR(100) NOT NULL COMMENT 'Loại cảnh báo cụ thể',
    `alert_title` VARCHAR(255) NOT NULL COMMENT 'Tiêu đề cảnh báo',
    `alert_description` TEXT COMMENT 'Mô tả chi tiết',
    
    -- ===============================
    -- SEVERITY & STATUS
    -- ===============================
    `severity_level` ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO') 
        NOT NULL DEFAULT 'MEDIUM' COMMENT 'Mức độ nghiêm trọng',
    
    `alert_status` ENUM('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED', 'DISMISSED') 
        NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái xử lý',
    
    `is_auto_generated` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Tự động hay thủ công',
    
    -- ===============================
    -- METRICS & MONITORING
    -- ===============================
    `metric_name` VARCHAR(100) COMMENT 'Tên metric',
    `current_value` DECIMAL(20,4) COMMENT 'Giá trị hiện tại',
    `threshold_value` DECIMAL(20,4) COMMENT 'Ngưỡng cảnh báo',
    `threshold_type` ENUM('ABOVE', 'BELOW', 'EQUAL') COMMENT 'Kiểu so sánh',
    
    -- ===============================
    -- SYSTEM CONTEXT
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
    -- ACTIONS & RESPONSES
    -- ===============================
    `suggested_action` TEXT COMMENT 'Hành động đề xuất',
    `manual_action_required` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Cần can thiệp thủ công',    -- ===============================
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
    -- BASIC TRACKING
    -- ===============================
    `notification_sent` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Đã gửi thông báo',
    `is_recurring` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Alert lặp lại',
    `occurrence_count` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Số lần xuất hiện',
    
    -- ===============================
    -- TIMESTAMPS
    -- ===============================
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',
    `expires_at` DATETIME COMMENT 'Thời gian hết hạn',
      `source_system` VARCHAR(100) COMMENT 'Hệ thống nguồn',
    `alert_version` VARCHAR(10) DEFAULT '1.0' COMMENT 'Phiên bản alert',
    
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
COMMENT='Hệ thống cảnh báo admin - Real-time monitoring';
