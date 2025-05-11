package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Notification;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Get all notifications for a user
    @GetMapping("/getNotificationsByUserId/{userId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Integer userId) {
        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    // Mark a notification as read
    @PostMapping("/markAsRead/{notificationId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification " + notificationId + " marked as read.");
    }
    
    // Mark all notifications as read for a user
    @PostMapping("/markAllAsRead/{userId}")
    public ResponseEntity<?> markAllAsRead(@PathVariable Integer userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications for user " + userId + " marked as read.");
    }
    
    // Get count of unread notifications
    @GetMapping("/unreadCount/{userId}")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Integer userId) {
        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }
}