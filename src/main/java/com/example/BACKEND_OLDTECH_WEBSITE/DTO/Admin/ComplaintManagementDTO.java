package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintManagementDTO {
    private Long id;
    private Long complainantId;
    private String complainantName;
    private Long complainedAgainstId;
    private String complainedAgainstName;
    private Long orderId;
    private Long productId;
    private String complaintType;
    private String title;
    private String description;
    private List<String> evidenceUrls;
    private String status;
    private String priority;
    private String adminNotes;
    private String resolution;
    private Long assignedTo;
    private String assignedToName;
    private String actionTaken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplaintActionRequest {
        private Long complaintId;
        private String action; // "investigate", "resolve", "dismiss", "escalate"
        private String resolution;
        private String actionTaken; // "none", "warning", "suspension", "ban", "product_removal", "order_refund"
        private String adminNotes;
    }
}
