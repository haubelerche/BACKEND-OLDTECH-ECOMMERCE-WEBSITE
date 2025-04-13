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
    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipientId;

    @Column(name = "sender_info", length = 100, nullable = false)
    private String senderInfo;

    @Column(name = "type", nullable = false)
    private NotificationTypeEnum type;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @Column(name = "link_url", length = 2048, nullable = false)
    private String linkUrl;

    @Column(name = "is_read", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isRead;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;
}