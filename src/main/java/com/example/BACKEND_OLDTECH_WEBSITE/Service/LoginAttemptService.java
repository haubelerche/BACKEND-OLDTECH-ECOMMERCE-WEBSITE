package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Service for tracking login attempts for security monitoring and fraud detection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final EntityManager entityManager;

    /**
     * Log a login attempt (success or failure)
     */
    @Transactional
    public void logLoginAttempt(String email, Integer userId, String ipAddress, 
                              String userAgent, boolean success, String failureReason,
                              String sessionId) {
        try {
            // First check if login_attempt table exists
            if (!isLoginAttemptTableExists()) {
                log.debug("Login attempt table does not exist, skipping login attempt logging");
                return;
            }

            String sql = "INSERT INTO login_attempt " +
                        "(user_id, email, ip_address, user_agent, attempt_time, success, failure_reason, session_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, userId);
            query.setParameter(2, email);
            query.setParameter(3, ipAddress);
            query.setParameter(4, userAgent);
            query.setParameter(5, Timestamp.valueOf(LocalDateTime.now()));
            query.setParameter(6, success);
            query.setParameter(7, failureReason);
            query.setParameter(8, sessionId);
            
            int result = query.executeUpdate();
            
            if (result > 0) {
                log.debug("Login attempt logged for email: {} from IP: {} - Success: {}", 
                         email, ipAddress, success);
            }
            
        } catch (Exception e) {
            log.warn("Failed to log login attempt for email: {} - Error: {}", email, e.getMessage());
        }
    }

    /**
     * Convenience method to log from HTTP request
     */
    @Transactional
    public void logLoginAttempt(String email, Integer userId, HttpServletRequest request, 
                              boolean success, String failureReason, String sessionId) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        logLoginAttempt(email, userId, ipAddress, userAgent, success, failureReason, sessionId);
    }

    /**
     * Check if login_attempt table exists
     */
    private boolean isLoginAttemptTableExists() {
        try {
            Query checkTableQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = 'login_attempt'");
            Number result = (Number) checkTableQuery.getSingleResult();
            return result.intValue() > 0;
        } catch (Exception e) {
            log.debug("Error checking login_attempt table existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get client IP address from request, considering proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // If multiple IPs are present (comma-separated), take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress != null ? ipAddress : "unknown";
    }

    /**
     * Get suspicious login attempts in the last specified hours
     */
    @Transactional(readOnly = true)
    public long getSuspiciousAttemptCount(String ipAddress, int hoursBack) {
        try {
            if (!isLoginAttemptTableExists()) {
                return 0;
            }

            String sql = "SELECT COUNT(*) FROM login_attempt " +
                        "WHERE ip_address = ? AND success = false " +
                        "AND attempt_time >= ?";
            
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, ipAddress);
            query.setParameter(2, Timestamp.valueOf(LocalDateTime.now().minusHours(hoursBack)));
            
            Number result = (Number) query.getSingleResult();
            return result != null ? result.longValue() : 0;
            
        } catch (Exception e) {
            log.warn("Error getting suspicious attempt count for IP: {} - Error: {}", ipAddress, e.getMessage());
            return 0;
        }
    }

    /**
     * Check if IP address should be blocked based on failed attempts
     */
    public boolean shouldBlockIpAddress(String ipAddress, int maxFailedAttempts, int hoursBack) {
        long failedAttempts = getSuspiciousAttemptCount(ipAddress, hoursBack);
        return failedAttempts >= maxFailedAttempts;
    }

    /**
     * Clean up old login attempts (for maintenance)
     */
    @Transactional
    public int cleanupOldLoginAttempts(int daysToKeep) {
        try {
            if (!isLoginAttemptTableExists()) {
                return 0;
            }

            String sql = "DELETE FROM login_attempt WHERE attempt_time < ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, Timestamp.valueOf(LocalDateTime.now().minusDays(daysToKeep)));
            
            int deletedCount = query.executeUpdate();
            log.info("Cleaned up {} old login attempt records older than {} days", deletedCount, daysToKeep);
            
            return deletedCount;
            
        } catch (Exception e) {
            log.error("Error cleaning up old login attempts: {}", e.getMessage());
            return 0;
        }
    }
}
