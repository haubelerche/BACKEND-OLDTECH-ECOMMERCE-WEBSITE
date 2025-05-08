package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TokenResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2LoginController {

    private final OAuth2Service oauth2Service;

    @GetMapping("/login")
    public ResponseEntity<String> login() {
        return ResponseEntity.ok("Please login through the frontend application");
    }

    @GetMapping("/success")
    public ResponseEntity<TokenResponse> success(OAuth2AuthenticationToken authentication) {
        return ResponseEntity.ok(oauth2Service.processOAuth2Login(authentication));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok(oauth2Service.getCurrentOAuth2UserInfo());
    }
}