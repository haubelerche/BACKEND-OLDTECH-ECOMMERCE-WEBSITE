package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    private Integer recipientId;

    private String senderInfo;

    private NotificationTypeEnum type;

    private String title;

    private String content;

    private String linkUrl;

    private Boolean isRead;

    private Timestamp createdAt;
}