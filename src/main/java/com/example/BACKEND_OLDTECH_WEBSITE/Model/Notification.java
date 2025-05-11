package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private User user;

    @Column(nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationTypeEnum notificationType = NotificationTypeEnum.System;

    @Column(nullable = false)
    private Timestamp createdAt;

    private boolean isRead = false;

    public Notification(User user, String message) {
        this.user = user;
        this.message = message;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.isRead = false;
    }
    
    public Notification(User user, String message, NotificationTypeEnum notificationType) {
        this.user = user;
        this.message = message;
        this.notificationType = notificationType;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.isRead = false;
    }
}