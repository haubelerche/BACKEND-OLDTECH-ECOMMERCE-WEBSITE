package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2UserInfo {
    private String name;
    private String email;
    private String picture;
} 