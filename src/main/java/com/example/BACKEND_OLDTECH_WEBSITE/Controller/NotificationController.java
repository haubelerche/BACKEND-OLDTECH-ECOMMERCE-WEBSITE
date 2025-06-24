package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notification.CreateNotificationRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notification.NotificationResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notification.NotificationStatsResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Notification;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    /*--- USER OPERATIONS ---*/

    /**
     * Lấy tất cả thông báo của người dùng hiện tại (sắp xếp theo thời gian mới nhất)
     */

    @PreAuthorize("hasAuthority('Customer')")
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyNotifications() {
        try {
            Integer userId = getCurrentUserId();
            List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", notifications);
            response.put("total", notifications.size());
            response.put("message", "Lấy thông báo thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting user notifications: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi lấy thông báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Lấy thông báo chưa đọc của người dùng hiện tại
     */

    @GetMapping("/my/unread")
    public ResponseEntity<Map<String, Object>> getMyUnreadNotifications() {
        try {
            Integer userId = getCurrentUserId();
            List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", notifications);
            response.put("unreadCount", notifications.size());
            response.put("message", "Lấy thông báo chưa đọc thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting unread notifications: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi lấy thông báo chưa đọc: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Đánh dấu một thông báo là đã đọc
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long notificationId) {
        try {
            Integer userId = getCurrentUserId();
            notificationService.markAsRead(notificationId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã đánh dấu thông báo là đã đọc");
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Bạn không có quyền truy cập thông báo này");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error marking notification as read: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi đánh dấu thông báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Đánh dấu tất cả thông báo là đã đọc
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        try {
            Integer userId = getCurrentUserId();
            notificationService.markAllAsRead(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã đánh dấu tất cả thông báo là đã đọc");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error marking all notifications as read: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi đánh dấu tất cả thông báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Lấy thống kê thông báo của người dùng hiện tại
     */
    @GetMapping("/my/stats")
    public ResponseEntity<Map<String, Object>> getMyNotificationStats() {
        try {
            Integer userId = getCurrentUserId();
            NotificationStatsResponse stats = notificationService.getUserNotificationStats(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", stats);
            response.put("message", "Lấy thống kê thông báo thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting notification stats: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi lấy thống kê thông báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /*--- Admin OPERATIONS ---*/

    /**
     * Gửi thông báo đến một người dùng cụ thể (Admin only)
     */
    @PostMapping("/send/user/{userId}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Map<String, Object>> sendNotificationToUser(
            @PathVariable Integer userId,
            @RequestBody CreateNotificationRequest request) {
        try {
            String senderInfo = "Admin - " + getCurrentUserEmail();
            Notification notification = notificationService.createNotificationForUser(userId, request, senderInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("notificationId", notification.getId());
            response.put("recipientId", userId);
            response.put("message", "Đã gửi thông báo đến người dùng thành công");
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Không tìm thấy người dùng với ID: " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error sending notification to user: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi gửi thông báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Gửi thông báo đến nhiều người dùng (Admin only)
     */
    @PostMapping("/send/users")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Map<String, Object>> sendNotificationToUsers(
            @RequestBody Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> userIds = (List<Integer>) requestBody.get("userIds");
            CreateNotificationRequest request = mapToCreateNotificationRequest(requestBody);
            
            String senderInfo = "Admin - " + getCurrentUserEmail();
            List<Notification> notifications = notificationService.createNotificationForUsers(userIds, request, senderInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("sentCount", notifications.size());
            response.put("requestedCount", userIds.size());
            response.put("message", String.format("Đã gửi thông báo đến %d/%d người dùng", 
                notifications.size(), userIds.size()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending notification to multiple users: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi gửi thông báo đến nhiều người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Gửi thông báo đến tất cả người dùng (Admin only)
     */
    @PostMapping("/send/all")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Map<String, Object>> sendNotificationToAllUsers(
            @RequestBody CreateNotificationRequest request) {
        try {
            String senderInfo = "Admin - " + getCurrentUserEmail();
            List<Notification> notifications = notificationService.createNotificationForAllUsers(request, senderInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("sentCount", notifications.size());
            response.put("message", String.format("Đã gửi thông báo đến %d người dùng", notifications.size()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending notification to all users: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi gửi thông báo đến tất cả người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Gửi thông báo đến người dùng theo vai trò (Admin only)
     */
    @PostMapping("/send/roles")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Map<String, Object>> sendNotificationToUsersByRoles(
            @RequestBody Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) requestBody.get("roles");
            CreateNotificationRequest request = mapToCreateNotificationRequest(requestBody);
            
            String senderInfo = "Admin - " + getCurrentUserEmail();
            List<Notification> notifications = notificationService.createNotificationForUsersByRoles(roles, request, senderInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("sentCount", notifications.size());
            response.put("targetRoles", roles);
            response.put("message", String.format("Đã gửi thông báo đến %d người dùng với vai trò: %s", 
                notifications.size(), String.join(", ", roles)));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending notification by roles: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi gửi thông báo theo vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /*--- AUTOMATIC NOTIFICATIONS ---*/

    /**
     * Gửi thông báo nhắc nhở hoàn thành hồ sơ (Admin only hoặc System)
     */
    @PostMapping("/send/profile-reminder/{userId}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Map<String, Object>> sendProfileCompletionReminder(@PathVariable Integer userId) {
        try {
            Notification notification = notificationService.sendProfileCompletionReminder(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("notificationId", notification.getId());
            response.put("message", "Đã gửi thông báo nhắc nhở hoàn thành hồ sơ");
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Không tìm thấy người dùng");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error sending profile completion reminder: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi gửi thông báo nhắc nhở: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Gửi thông báo xác minh tài khoản (Admin only)
     */
    @PostMapping("/send/verification/{userId}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Map<String, Object>> sendAccountVerificationNotification(
            @PathVariable Integer userId,
            @RequestParam boolean approved) {
        try {
            Notification notification = notificationService.sendAccountVerificationNotification(userId, approved);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("notificationId", notification.getId());
            response.put("approved", approved);
            response.put("message", approved ? 
                "Đã gửi thông báo xác minh thành công" : 
                "Đã gửi thông báo từ chối xác minh");
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Không tìm thấy người dùng");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error sending verification notification: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi gửi thông báo xác minh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    /*--- HELPER METHODS ---*/

    /**
     * Lấy ID người dùng hiện tại từ Authentication context
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Người dùng chưa đăng nhập");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.example.BACKEND_OLDTECH_WEBSITE.Model.User) {
            return ((com.example.BACKEND_OLDTECH_WEBSITE.Model.User) principal).getUserId();
        }
        
        throw new SecurityException("Không thể xác định người dùng hiện tại");
    }

    /**
     * Lấy email người dùng hiện tại từ Authentication context
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "Unknown";
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.example.BACKEND_OLDTECH_WEBSITE.Model.User) {
            return ((com.example.BACKEND_OLDTECH_WEBSITE.Model.User) principal).getEmail();
        }
        
        return authentication.getName();
    }

    /**
     * Chuyển đổi Map thành CreateNotificationRequest object
     */
    private CreateNotificationRequest mapToCreateNotificationRequest(Map<String, Object> requestBody) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        
        if (requestBody.containsKey("notificationType")) {
            // Handle enum conversion
            String typeString = (String) requestBody.get("notificationType");
            try {
                request.setNotificationType(
                    com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum.valueOf(typeString));            } catch (IllegalArgumentException e) {
                request.setNotificationType(
                    com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum.ADMIN_ANNOUNCEMENT);
            }
        }
        
        if (requestBody.containsKey("title")) {
            request.setTitle((String) requestBody.get("title"));
        }
        
        if (requestBody.containsKey("content")) {
            request.setContent((String) requestBody.get("content"));
        }
        
        if (requestBody.containsKey("linkUrl")) {
            request.setLinkUrl((String) requestBody.get("linkUrl"));
        }
        
        return request;
    }
}