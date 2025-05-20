package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class VerificationResponse {
    private Integer verifyId;
    private Integer userId;
    private Boolean isApproved;
    private String adminResponse;
    private String selfiePicUrl;
    private String frontImageUrl;
    private String backImageUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}