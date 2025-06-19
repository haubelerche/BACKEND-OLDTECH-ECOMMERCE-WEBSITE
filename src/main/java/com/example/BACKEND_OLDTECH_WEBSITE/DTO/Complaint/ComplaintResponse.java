package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatus;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class ComplaintResponse {
    private Long complaintId;
    private Integer orderId;
    private Integer respondentId;
    private String respondentName;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String adminResponse;
    private String reason;
    private ComplaintStatus status;
    private String statusDisplay;
}
