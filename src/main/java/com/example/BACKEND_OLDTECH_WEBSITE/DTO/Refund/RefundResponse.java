package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Refund;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class RefundResponse {
    private Integer refundId;
    private Integer orderId;
    private Integer userId;
    private String reason;
    private RefundStatusEnum status;
    private String adminNotes;
    private Timestamp requestedAt;
    private Timestamp updatedAt;
}