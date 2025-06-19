package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notification.CreateNotificationRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notification.NotificationResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notification.NotificationStatsResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Notification;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.NotificationRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;

    // ===== USER OPERATIONS =====
    
    /**
     * Lấy tất cả thông báo của người dùng (sắp xếp theo thời gian mới nhất)
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Integer userId) {
        logger.info("Getting all notifications for user ID: {}", userId);
        
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy thông báo chưa đọc của người dùng
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Integer userId) {
        logger.info("Getting unread notifications for user ID: {}", userId);
        
        List<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Đánh dấu một thông báo là đã đọc
     */
    @Transactional
    public void markAsRead(Long notificationId, Integer userId) {
        logger.info("Marking notification {} as read for user {}", notificationId, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông báo với ID: " + notificationId));
        
        // Kiểm tra quyền sở hữu
        if (!notification.getRecipientId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền truy cập thông báo này");
        }
        
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Timestamp.from(Instant.now()));
            notificationRepository.save(notification);
            logger.info("Notification {} marked as read", notificationId);
        }
    }
    
    /**
     * Đánh dấu tất cả thông báo là đã đọc
     */
    @Transactional
    public void markAllAsRead(Integer userId) {
        logger.info("Marking all notifications as read for user {}", userId);
        
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndIsReadFalse(userId);
        Timestamp now = Timestamp.from(Instant.now());
        
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(now);
        }
        
        notificationRepository.saveAll(unreadNotifications);
        logger.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
    }
    
    /**
     * Lấy thống kê thông báo của người dùng
     */
    @Transactional(readOnly = true)
    public NotificationStatsResponse getUserNotificationStats(Integer userId) {
        logger.info("Getting notification stats for user {}", userId);
        
        long totalNotifications = notificationRepository.countByRecipientId(userId);
        long unreadNotifications = notificationRepository.countByRecipientIdAndIsReadFalse(userId);
        long readNotifications = totalNotifications - unreadNotifications;
        
        return NotificationStatsResponse.builder()
                .totalNotifications(totalNotifications)
                .unreadNotifications(unreadNotifications)
                .readNotifications(readNotifications)
                .build();
    }

    // ===== ADMIN OPERATIONS =====
      /**
     * Tạo thông báo cho một người dùng cụ thể
     */
    @Transactional
    public Notification createNotificationForUser(Integer userId, CreateNotificationRequest request, String senderInfo) {
        logger.info("Creating notification for user {} by {}", userId, senderInfo);
        
        // Kiểm tra user tồn tại
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }
        
        Notification notification = Notification.builder()
                .recipientId(userId)
                .senderInfo(senderInfo)
                .notificationType(request.getNotificationType())
                .title(request.getTitle())
                .content(request.getContent())
                .linkUrl(request.getLinkUrl())
                .isRead(false)
                .createdAt(Timestamp.from(Instant.now()))
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        logger.info("Created notification {} for user {}", savedNotification.getId(), userId);
        
        return savedNotification;
    }
    
    /**
     * Tạo thông báo cho nhiều người dùng
     */
    @Transactional
    public List<Notification> createNotificationForUsers(List<Integer> userIds, CreateNotificationRequest request, String senderInfo) {
        logger.info("Creating notification for {} users by {}", userIds.size(), senderInfo);
        
        List<Notification> notifications = new ArrayList<>();
        Timestamp now = Timestamp.from(Instant.now());
        
        for (Integer userId : userIds) {
            try {
                // Kiểm tra user tồn tại
                if (userRepository.existsById(userId)) {
                    Notification notification = Notification.builder()
                            .recipientId(userId)
                            .senderInfo(senderInfo)
                            .notificationType(request.getNotificationType())
                            .title(request.getTitle())
                            .content(request.getContent())
                            .linkUrl(request.getLinkUrl())
                            .isRead(false)
                            .createdAt(now)
                            .build();
                    
                    notifications.add(notification);
                } else {
                    logger.warn("User {} not found, skipping notification", userId);
                }
            } catch (Exception e) {
                logger.error("Error creating notification for user {}: {}", userId, e.getMessage());
            }
        }
        
        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        logger.info("Created {} notifications for users", savedNotifications.size());
        
        return savedNotifications;
    }
    
    /**
     * Tạo thông báo cho tất cả người dùng
     */
    @Transactional
    public List<Notification> createNotificationForAllUsers(CreateNotificationRequest request, String senderInfo) {
        logger.info("Creating notification for all users by {}", senderInfo);
        
        List<User> allUsers = userRepository.findAll();
        List<Integer> userIds = allUsers.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        
        return createNotificationForUsers(userIds, request, senderInfo);
    }
    
    /**
     * Tạo thông báo cho người dùng theo vai trò
     */
    @Transactional
    public List<Notification> createNotificationForUsersByRoles(List<String> roles, CreateNotificationRequest request, String senderInfo) {
        logger.info("Creating notification for users with roles {} by {}", roles, senderInfo);
        
        List<RoleEnum> roleEnums = roles.stream()
                .map(role -> {
                    try {
                        return RoleEnum.valueOf(role);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid role: {}", role);
                        return null;
                    }
                })
                .filter(role -> role != null)
                .collect(Collectors.toList());
        
        List<User> users = userRepository.findByRoleIn(roleEnums);
        List<Integer> userIds = users.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        
        return createNotificationForUsers(userIds, request, senderInfo);
    }

    // ===== AUTOMATIC NOTIFICATIONS =====
      /**
     * Gửi thông báo nhắc nhở hoàn thành hồ sơ
     */
    @Transactional
    public Notification sendProfileCompletionReminder(Integer userId) {
        logger.info("Sending profile completion reminder to user {}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }
        
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setNotificationType(NotificationTypeEnum.PROFILE_INCOMPLETE);
        request.setTitle("Nhắc nhở hoàn thành hồ sơ");
        request.setContent("Vui lòng hoàn thành thông tin cá nhân để được cấp đầy đủ quyền mua sắp và bán hàng. " +
                          "Hồ sơ đầy đủ sẽ giúp tăng độ tin cậy và mở rộng quyền hạn của bạn trên nền tảng.");
        request.setLinkUrl("/profile/edit");
        
        return createNotificationForUser(userId, request, "System");
    }
      /**
     * Gửi thông báo xác minh tài khoản
     */
    @Transactional
    public Notification sendAccountVerificationNotification(Integer userId, boolean approved) {
        logger.info("Sending account verification notification to user {}, approved: {}", userId, approved);
        
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }
        
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setNotificationType(NotificationTypeEnum.ACCOUNT_VERIFICATION);
        
        if (approved) {
            request.setTitle("Tài khoản đã được xác minh");
            request.setContent("Chúc mừng! Tài khoản của bạn đã được xác minh thành công. " +
                              "Bạn giờ đây có thể sử dụng đầy đủ các tính năng mua sắm và bán hàng trên nền tảng.");
            request.setLinkUrl("/dashboard");
        } else {
            request.setTitle("Yêu cầu xác minh bị từ chối");
            request.setContent("Rất tiếc, yêu cầu xác minh tài khoản của bạn đã bị từ chối. " +
                              "Vui lòng kiểm tra lại thông tin và tài liệu, sau đó gửi lại yêu cầu xác minh.");
            request.setLinkUrl("/profile/verification");
        }
        
        return createNotificationForUser(userId, request, "Admin");
    }
    
    /**
     * Gửi thông báo chào mừng cho người dùng mới
     */
    @Transactional
    public Notification sendWelcomeNotification(Integer userId) {
        logger.info("Sending welcome notification to user {}", userId);
        
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setNotificationType(NotificationTypeEnum.SYSTEM);
        request.setTitle("Chào mừng đến với OldTech!");
        request.setContent("Chào mừng bạn đến với OldTech! Cảm ơn bạn đã đăng ký tài khoản. " +
                          "Để sử dụng đầy đủ các tính năng, vui lòng hoàn thành thông tin cá nhân và xác minh tài khoản.");
        request.setLinkUrl("/profile/complete");
        
        return createNotificationForUser(userId, request, "System");
    }
    
    /**
     * Gửi thông báo cập nhật đơn hàng
     */
    @Transactional
    public Notification sendOrderUpdateNotification(Integer userId, String orderStatus, Integer orderId) {
        logger.info("Sending order update notification to user {} for order {}", userId, orderId);
        
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setNotificationType(NotificationTypeEnum.ORDER_UPDATE);
        request.setTitle("Cập nhật đơn hàng #" + orderId);
        request.setContent("Đơn hàng #" + orderId + " của bạn đã được cập nhật trạng thái: " + orderStatus);
        request.setLinkUrl("/orders/" + orderId);
        
        return createNotificationForUser(userId, request, "System");
    }

    // ===== HELPER METHODS =====
      /**
     * Chuyển đổi Notification entity thành NotificationResponse DTO
     */
    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .senderInfo(notification.getSenderInfo())
                .notificationType(notification.getNotificationType())
                .notificationTypeDisplay(notification.getNotificationType() != null ? 
                    notification.getNotificationType().getDisplayName() : "")
                .title(notification.getTitle())
                .content(notification.getContent())
                .linkUrl(notification.getLinkUrl())
                .isRead(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .timeAgo(calculateTimeAgo(notification.getCreatedAt()))
                .build();
    }
    
    /**
     * Tính toán thời gian "time ago" từ timestamp
     */
    private String calculateTimeAgo(Timestamp timestamp) {
        if (timestamp == null) return "";
        
        long diffInMillis = System.currentTimeMillis() - timestamp.getTime();
        long diffInSeconds = diffInMillis / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;
        
        if (diffInDays > 0) {
            return diffInDays + " ngày trước";
        } else if (diffInHours > 0) {
            return diffInHours + " giờ trước";
        } else if (diffInMinutes > 0) {
            return diffInMinutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }
    
    /**
     * Tạo thông báo đơn giản (backward compatibility)
     */
    @Transactional
    public Notification createSimpleNotification(Integer userId, String message, NotificationTypeEnum type) {
        logger.info("Creating simple notification for user {}", userId);
        
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setNotificationType(type);
        request.setTitle(type.getDisplayName());
        request.setContent(message);
        
        return createNotificationForUser(userId, request, "System");
    }
    
    /**
     * Xóa thông báo cũ (older than specified days)
     */
    @Transactional
    public void cleanupOldNotifications(int daysOld) {
        logger.info("Cleaning up notifications older than {} days", daysOld);
        
        Timestamp cutoffDate = Timestamp.from(Instant.now().minusSeconds(daysOld * 24 * 60 * 60));
        List<Notification> oldNotifications = notificationRepository.findByCreatedAtBefore(cutoffDate);
        
        notificationRepository.deleteAll(oldNotifications);
        logger.info("Deleted {} old notifications", oldNotifications.size());
    }
}