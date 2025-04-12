package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "notification")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipientId;

    @Column(name = "sender_info", length = 100)
    private String senderInfo;

    @Column(name = "type")
    private NotificationTypeEnum type;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "link_url", length = 2048)
    private String linkUrl;

    @Column(name = "is_read", columnDefinition = "TINYINT(1)")
    private Boolean isRead;

    @Column(name = "created_at")
    private Timestamp createdAt;
}