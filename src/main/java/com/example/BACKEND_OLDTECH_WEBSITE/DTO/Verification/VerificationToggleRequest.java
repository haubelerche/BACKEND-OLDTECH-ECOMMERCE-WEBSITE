package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification;

import lombok.Data;

@Data
public class VerificationToggleRequest {
    private Boolean isApproved;
    private String adminResponse;
}
