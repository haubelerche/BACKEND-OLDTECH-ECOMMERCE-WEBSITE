package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notification;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateNotificationRequest {
    
    @NotNull(message = "Loại thông báo không được để trống")
    private NotificationTypeEnum notificationType;
    
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;
    
    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 2000, message = "Nội dung không được vượt quá 2000 ký tự")
    private String content;
    
    // Optional link for action
    @Size(max = 500, message = "Link không được vượt quá 500 ký tự")
    private String linkUrl;
    
    // For single user notification
    private Integer recipientId;
    
    // For multiple users notification
    private List<Integer> recipientIds;
    
    // For sending to all users of specific roles
    private List<String> recipientRoles; // Customer, Seller, Admin
    
    // For sending to all users
    private boolean sendToAll = false;
}
