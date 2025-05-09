package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Bắt buộc nhập refresh token")
    private String refreshToken;
} 