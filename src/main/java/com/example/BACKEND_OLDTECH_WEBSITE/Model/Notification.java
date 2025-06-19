package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "recipient_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer recipientId;

    @Column(name = "sender_info", length = 255)
    private String senderInfo; // Info about who sent (Admin name, System, etc.)

    @Column(name = "notification_type", length = 50) // Increased column length to handle longer enum names
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationTypeEnum notificationType = NotificationTypeEnum.SYSTEM;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "link_url", length = 500)
    private String linkUrl; // Optional link for action

    @Column(name = "is_read", columnDefinition = "TINYINT(1) default '0'")
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "read_at")
    private Timestamp readAt;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", referencedColumnName = "user_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User recipient;

    // Backward compatibility methods
    public String getMessage() {
        return this.content;
    }

    public void setMessage(String message) {
        this.content = message;
    }

    public User getUser() {
        return this.recipient;
    }

    public void setUser(User user) {
        if (user != null) {
            this.recipientId = user.getUserId();
            this.recipient = user;
        }
    }

    public void setRead(boolean read) {
        this.isRead = read;
        if (read && this.readAt == null) {
            this.readAt = new Timestamp(System.currentTimeMillis());
        } else if (!read) {
            this.readAt = null;
        }
    }

    // Constructor for backward compatibility
    public Notification(User user, String message) {
        this.recipientId = user.getUserId();
        this.recipient = user;
        this.title = "Thông báo";
        this.content = message;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.isRead = false;
        this.notificationType = NotificationTypeEnum.SYSTEM;
    }

    // Constructor for backward compatibility
    public Notification(User user, String message, NotificationTypeEnum notificationType) {
        this.recipientId = user.getUserId();
        this.recipient = user;
        this.title = notificationType.getDisplayName();
        this.content = message;
        this.notificationType = notificationType;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.isRead = false;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Timestamp(System.currentTimeMillis());
        }
    }
}