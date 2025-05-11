package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TokenResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2UserInfo;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.OAuth2Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> login() {
        log.info("OAuth2 login initiated");
        return ResponseEntity.ok(Map.of(
            "google", "/oauth2/authorization/google",
            "facebook", "/oauth2/authorization/facebook",
            "message", "Use these URLs to authenticate with OAuth2 providers"
        ));
    }

    @GetMapping("/success")
    public ResponseEntity<TokenResponse> success(OAuth2AuthenticationToken authentication) {
        log.info("OAuth2 authentication successful with provider: {}", 
                authentication.getAuthorizedClientRegistrationId());
        return ResponseEntity.ok(oauth2Service.processOAuth2Login(authentication));
    }

    @GetMapping("/user")
    public ResponseEntity<OAuth2UserInfo> getCurrentUser() {
        OAuth2UserInfo userInfo = oauth2Service.getCurrentOAuth2UserInfo();
        return ResponseEntity.ok(userInfo);
    }
    

    @PostMapping("/register/{userId}")
    public ResponseEntity<TokenResponse> register(
            @PathVariable Integer userId,
            @Valid @RequestBody OAuth2RegisterRequest request) {
        log.info("Processing OAuth2 registration for userId: {}", userId);
        try {
            TokenResponse response = oauth2Service.completeOAuth2Registration(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi đăng ký {}: {}", userId, e.getMessage());
            throw e;
        }
    }
    

    @GetMapping("/failure")
    public ResponseEntity<Map<String, String>> failure(@RequestParam(required = false) String error) {
        log.error("OAuth2 authentication failed: {}", error);
        return ResponseEntity.badRequest().body(Map.of(
            "message", "Authentication failed",
            "error", error != null ? error : "Unknown error"
        ));
    }
}
// remember to create register thru oauth2