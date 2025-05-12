package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "recipient_id")
    private Integer recipientId;

    @Column(name = "sender_info", length = 255)
    private String senderInfo;

    @Column(name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationTypeEnum notificationType = NotificationTypeEnum.System;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", length = 255)
    private String content;

    @Column(name = "link_url", length = 255)
    private String linkUrl;

    @Column(name = "is_read", columnDefinition = "TINYINT(1) default '0'")
    private boolean isRead = false;

    @Column(name = "read_at")
    private Timestamp readAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    // Temporary method to maintain compatibility with existing code
    // This should be updated in all services that use this entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", insertable = false, updatable = false)
    private User user;

    // Use content as message for backward compatibility
    public String getMessage() {
        return this.content;
    }

    public void setMessage(String message) {
        this.content = message;
    }

    // Constructor for backward compatibility
    public Notification(User user, String message) {
        this.recipientId = user.getUserId();
        this.content = message;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.isRead = false;
    }
    
    // Constructor for backward compatibility
    public Notification(User user, String message, NotificationTypeEnum notificationType) {
        this.recipientId = user.getUserId();
        this.content = message;
        this.notificationType = notificationType;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.isRead = false;
    }
}