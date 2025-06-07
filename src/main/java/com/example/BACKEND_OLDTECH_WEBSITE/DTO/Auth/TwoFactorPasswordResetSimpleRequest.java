package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TwoFactorPasswordResetSimpleRequest {
    private String email;
    private String code;
    private String newPassword;
}
