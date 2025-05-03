package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notifs;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class NotificationDTO {
    private Integer notificationId;
    private Integer recipientId;
    private String senderInfo;
    private String type;
    private String title;
    private String content;
    private String linkUrl;
    private Boolean isRead;
    private Timestamp read_at;
    private Timestamp created_at;
}