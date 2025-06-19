package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notification;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Integer recipientId;
    private String recipientName;
    private String senderInfo;
    private NotificationTypeEnum notificationType;
    private String notificationTypeDisplay;
    private String title;
    private String content;
    private String linkUrl;
    private boolean isRead;
    private Timestamp readAt;
    private Timestamp createdAt;
    private String timeAgo; // Human readable time like "2 hours ago"
}
