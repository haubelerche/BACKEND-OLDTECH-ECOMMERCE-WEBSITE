package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TwoFactorPasswordResetRequest {
    private String email;
    private String token;
    private String newPassword;

}
