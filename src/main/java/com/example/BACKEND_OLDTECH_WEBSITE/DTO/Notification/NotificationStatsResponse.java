package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsResponse {
    private long totalNotifications;
    private long unreadNotifications;
    private long readNotifications;
    private long todayNotifications;
    private long thisWeekNotifications;
    private long thisMonthNotifications;
}
