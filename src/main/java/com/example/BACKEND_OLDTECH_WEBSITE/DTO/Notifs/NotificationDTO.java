package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notifs;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class NotificationDTO {
    private Integer notificationId;
    private Integer recipientId;
    private String senderInfo;
    private NotificationTypeEnum notificationType;
    private String title;
    private String content;
    private String linkUrl;
    private Boolean isRead;
    private Timestamp read_at;
    private Timestamp created_at;
}