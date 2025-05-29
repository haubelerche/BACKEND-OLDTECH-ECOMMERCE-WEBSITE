package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TokenResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2UserInfo;
import com.example.BACKEND_OLDTECH_WEBSITE.Exception.BadRequestException;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.OAuth2Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    @Value("${app.base-url}")
    private String baseUrl;


    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> login() {
        log.info("OAuth2 login initiated");
        Map<String, Object> response = new HashMap<>();

        Map<String, String> providers = new HashMap<>();
        providers.put("google", baseUrl + "/oauth2/authorization/google");
        providers.put("facebook", baseUrl + "/oauth2/authorization/facebook");

        response.put("providers", providers);
        response.put("message", "Use these URLs to authenticate with OAuth2 providers");
        response.put("instructions", "After successful authentication, complete your profile if needed");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/register-options")
    public ResponseEntity<Map<String, Object>> getRegisterOptions() {
        log.info("OAuth2 registration options requested");
        Map<String, Object> response = new HashMap<>();

        Map<String, String> providers = new HashMap<>();
        providers.put("google", baseUrl + "/oauth2/authorization/google");
        providers.put("facebook", baseUrl + "/oauth2/authorization/facebook");

        response.put("providers", providers);
        response.put("message", "Use these URLs to register with OAuth2 providers");
        response.put("flowDescription", "1. Click on provider URL to authenticate" +
                                       "2. After successful authentication, you'll be redirected to complete your profile if needed" +
                                       "3. Fill in additional required information to complete registration");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/success")
    public ResponseEntity<TokenResponse> success(OAuth2AuthenticationToken authentication) {
        log.info("OAuth2 authentication successful with provider: {}", 
                authentication.getAuthorizedClientRegistrationId());
        return ResponseEntity.ok(oauth2Service.processOAuth2Login(authentication));
    }


    @GetMapping("/failure")
    public ResponseEntity<Map<String, Object>> failure(@RequestParam(required = false) String error) {
        log.error("OAuth2 authentication failed: {}", error);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Authentication failed");

        // Enhanced error handling with more specific messages
        if (error != null) {
            response.put("error", error);

            // Provide more specific guidance based on common OAuth2 errors
            if (error.contains("facebook") || error.contains("Facebook")) {
                response.put("provider", "Facebook");
                response.put("helpFacebook", "Facebook authentication issues may be due to: "
                    + "1. Invalid permissions requested "
                    + "2. Facebook account issues "
                    + "3. Backend configuration problems");
            } else if (error.contains("google") || error.contains("Google")) {
                response.put("provider", "Google");
            }
        } else {
            response.put("error", "Unknown error");
        }

        response.put("help", "Please try again or use a different authentication method");
        response.put("alternativeMethod", "You can register using email and password instead");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }


    @GetMapping("/user")
    public ResponseEntity<OAuth2UserInfo> getCurrentUser() {
        OAuth2UserInfo userInfo = oauth2Service.getCurrentOAuth2UserInfo();
        return ResponseEntity.ok(userInfo);
    }
    

    @PostMapping("/register/{userId}")
    public ResponseEntity<TokenResponse> completeRegistration(
            @PathVariable Integer userId,
            @Valid @RequestBody OAuth2RegisterRequest request) {
        log.info("Processing OAuth2 registration completion for userId: {}", userId);
        try {
            TokenResponse response = oauth2Service.completeOAuth2Registration(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during OAuth2 registration for userId {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerInitial(@RequestBody(required = false) Map<String, Object> payload) {
        log.info("Initial OAuth2 registration request received");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "To register with OAuth2, please use the provider links below");

        Map<String, String> providers = new HashMap<>();
        providers.put("google", baseUrl + "/oauth2/authorization/google");
        providers.put("facebook", baseUrl + "/oauth2/authorization/facebook");

        response.put("providers", providers);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/registration-status/{userId}")
    public ResponseEntity<Map<String, Object>> getRegistrationStatus(@PathVariable Integer userId) {
        log.info("Checking OAuth2 registration status for userId: {}", userId);

        Map<String, Object> status = oauth2Service.getRegistrationStatus(userId);
        return ResponseEntity.ok(status);
    }


    @GetMapping("/error")
    public ResponseEntity<Map<String, String>> handleOAuth2Error(@RequestParam(required = false) String error) {
        log.error("OAuth2 authentication error: {}", error);

        Map<String, String> response = new HashMap<>();
        response.put("error", error != null ? error : "Authentication failed");
        response.put("message", "OAuth2 authentication failed. Please try again or use email/password registration.");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
