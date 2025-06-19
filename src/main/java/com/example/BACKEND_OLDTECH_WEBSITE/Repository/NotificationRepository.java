package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find by recipient ID - ordered by creation time (newest first)
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);
    
    // Find unread notifications for a user
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Integer recipientId);
    
    // Find read notifications for a user
    List<Notification> findByRecipientIdAndIsReadTrueOrderByCreatedAtDesc(Integer recipientId);
    
    // Find by notification type
    List<Notification> findByRecipientIdAndNotificationTypeOrderByCreatedAtDesc(
        Integer recipientId, NotificationTypeEnum notificationType);
    
    // Count unread notifications for a user
    long countByRecipientIdAndIsReadFalse(Integer recipientId);
    
    // Find notifications by date range
    List<Notification> findByRecipientIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Integer recipientId, Timestamp startDate, Timestamp endDate);
    
    // Find notifications by type and read status
    List<Notification> findByRecipientIdAndNotificationTypeAndIsReadOrderByCreatedAtDesc(
        Integer recipientId, NotificationTypeEnum notificationType, boolean isRead);
      // Custom query to find recent notifications (last N days)
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId " +
           "AND n.createdAt >= :sinceDate ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("recipientId") Integer recipientId, 
                                             @Param("sinceDate") Timestamp sinceDate);
    
    // Additional methods for NotificationService
    List<Notification> findByRecipientIdAndIsReadFalse(Integer recipientId);
    long countByRecipientId(Integer recipientId);
    List<Notification> findByCreatedAtBefore(Timestamp cutoffDate);
    
    // Admin queries - for statistics and monitoring
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.createdAt >= :sinceDate")
    long countNotificationsSince(@Param("sinceDate") Timestamp sinceDate);
    
    @Query("SELECT n.notificationType, COUNT(n) FROM Notification n " +
           "WHERE n.createdAt >= :sinceDate GROUP BY n.notificationType")
    List<Object[]> getNotificationTypeStatistics(@Param("sinceDate") Timestamp sinceDate);
}