package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponse {
    private String token;
    private User user;
    private int expiresIn;
    private String tokenType;
    private Boolean requiresProfileCompletion;
}

