package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
    private String token;
    private User user;
} 