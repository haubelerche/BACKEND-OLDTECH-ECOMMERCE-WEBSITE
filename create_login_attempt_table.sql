-- ===============================
-- LOGIN ATTEMPT TABLE
-- ===============================
-- Table to track login attempts for security monitoring and fraud detection

CREATE TABLE IF NOT EXISTS `login_attempt` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL,
    `email` VARCHAR(255) NOT NULL,
    `ip_address` VARCHAR(45) NOT NULL,
    `user_agent` TEXT NULL,
    `attempt_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `success` BOOLEAN NOT NULL DEFAULT FALSE,
    `failure_reason` VARCHAR(100) NULL COMMENT 'e.g., INVALID_PASSWORD, USER_NOT_FOUND, ACCOUNT_LOCKED',
    `session_id` VARCHAR(255) NULL,
    `location_info` JSON NULL COMMENT 'Geolocation data if available',
    
    -- Indexes for performance
    INDEX `idx_email_attempt_time` (`email`, `attempt_time`),
    INDEX `idx_ip_attempt_time` (`ip_address`, `attempt_time`),
    INDEX `idx_success_attempt_time` (`success`, `attempt_time`),
    INDEX `idx_user_id_attempt_time` (`user_id`, `attempt_time`),
    
    -- Foreign key constraint (optional)
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- SAMPLE DATA FOR TESTING
-- ===============================

-- Insert some sample login attempts for testing fraud detection
INSERT INTO `login_attempt` (
    `user_id`, `email`, `ip_address`, `user_agent`, `attempt_time`, `success`, `failure_reason`
) VALUES 
-- Successful logins
(1, 'user1@example.com', '192.168.1.100', 'Mozilla/5.0', NOW() - INTERVAL 1 HOUR, TRUE, NULL),
(2, 'user2@example.com', '192.168.1.101', 'Mozilla/5.0', NOW() - INTERVAL 2 HOUR, TRUE, NULL),

-- Failed login attempts (potential brute force)
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 30 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 29 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 28 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 27 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 26 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 25 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 24 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 23 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 22 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 21 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 20 MINUTE, FALSE, 'INVALID_PASSWORD'),
(NULL, 'admin@example.com', '10.0.0.1', 'Suspicious Bot', NOW() - INTERVAL 19 MINUTE, FALSE, 'INVALID_PASSWORD'),

-- Multiple failed attempts from different IPs
(NULL, 'test@example.com', '10.0.0.2', 'Another Bot', NOW() - INTERVAL 15 MINUTE, FALSE, 'USER_NOT_FOUND'),
(NULL, 'test@example.com', '10.0.0.3', 'Third Bot', NOW() - INTERVAL 14 MINUTE, FALSE, 'USER_NOT_FOUND'),
(NULL, 'test@example.com', '10.0.0.4', 'Fourth Bot', NOW() - INTERVAL 13 MINUTE, FALSE, 'USER_NOT_FOUND');

-- ===============================
-- UTILITY PROCEDURES
-- ===============================

DELIMITER ;;

-- Procedure to clean up old login attempts (for data retention)
CREATE PROCEDURE CleanupOldLoginAttempts()
BEGIN
    DECLARE deleted_count INT DEFAULT 0;
    
    DELETE FROM login_attempt 
    WHERE attempt_time < DATE_SUB(NOW(), INTERVAL 90 DAY);
    
    SET deleted_count = ROW_COUNT();
    SELECT CONCAT('Deleted ', deleted_count, ' old login attempt records') as result;
END;;

-- Procedure to get login attempt statistics
CREATE PROCEDURE GetLoginAttemptStats(IN hours_back INT)
BEGIN
    SELECT 
        DATE(attempt_time) as attempt_date,
        HOUR(attempt_time) as attempt_hour,
        success,
        COUNT(*) as attempt_count,
        COUNT(DISTINCT ip_address) as unique_ips,
        COUNT(DISTINCT email) as unique_emails
    FROM login_attempt 
    WHERE attempt_time >= DATE_SUB(NOW(), INTERVAL hours_back HOUR)
    GROUP BY DATE(attempt_time), HOUR(attempt_time), success
    ORDER BY attempt_date DESC, attempt_hour DESC;
END;;

-- Procedure to identify suspicious IP addresses
CREATE PROCEDURE GetSuspiciousIPs(IN hours_back INT, IN min_failed_attempts INT)
BEGIN
    SELECT 
        ip_address,
        COUNT(*) as failed_attempts,
        COUNT(DISTINCT email) as unique_emails_targeted,
        MIN(attempt_time) as first_attempt,
        MAX(attempt_time) as last_attempt,
        GROUP_CONCAT(DISTINCT email SEPARATOR ', ') as targeted_emails
    FROM login_attempt 
    WHERE attempt_time >= DATE_SUB(NOW(), INTERVAL hours_back HOUR)
    AND success = FALSE
    GROUP BY ip_address
    HAVING COUNT(*) >= min_failed_attempts
    ORDER BY failed_attempts DESC;
END;;

DELIMITER ;

-- ===============================
-- GRANTS (OPTIONAL)
-- ===============================

-- Grant permissions to application user (adjust as needed)
-- GRANT SELECT, INSERT, UPDATE ON login_attempt TO 'app_user'@'localhost';
-- GRANT EXECUTE ON PROCEDURE CleanupOldLoginAttempts TO 'app_user'@'localhost';
-- GRANT EXECUTE ON PROCEDURE GetLoginAttemptStats TO 'app_user'@'localhost';
-- GRANT EXECUTE ON PROCEDURE GetSuspiciousIPs TO 'app_user'@'localhost';

-- ===============================
-- END OF SCRIPT
-- ===============================
