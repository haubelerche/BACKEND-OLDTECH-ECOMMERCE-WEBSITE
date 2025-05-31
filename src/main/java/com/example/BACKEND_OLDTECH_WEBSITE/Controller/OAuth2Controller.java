package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TokenResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2UserInfo;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.OAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.Cookie;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/oauth2/login")
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

    @GetMapping(value = "/oauth2/register-options", produces = "application/json")
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

    @GetMapping("/oauth2/user")
    public ResponseEntity<OAuth2UserInfo> getCurrentUser() {
        OAuth2UserInfo userInfo = oauth2Service.getCurrentOAuth2UserInfo();
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/oauth2/register/{userId}")
    public ResponseEntity<TokenResponse> register(
            @PathVariable Integer userId,
            @Valid @RequestBody OAuth2RegisterRequest request) {
        log.info("Processing OAuth2 registration for userId: {}", userId);
        try {
            TokenResponse response = oauth2Service.completeOAuth2Registration(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during registration {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/oauth2/register")
    public ResponseEntity<Map<String, Object>> registerInitial(@RequestBody(required = false) Map<String, Object> payload) {
        log.info("Initial OAuth2 registration request received");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "To register with OAuth2, please use the provider links below");

        Map<String, String> providers = new HashMap<>();
        providers.put("google", baseUrl + "/oauth2/authorization/google");
        providers.put("facebook", baseUrl + "/oauth2/authorization/facebook");

        response.put("providers", providers);
        response.put("instructions", "After successful authentication, you'll need to complete your registration");

        return ResponseEntity.ok(response);
    }

    // HANDLING OAUTH2 SUCCESS AND FAILURE
    @GetMapping("/oauth2/failure")
    public ResponseEntity<Map<String, String>> failure(@RequestParam(required = false) String error,
                                                     @RequestParam(required = false) String provider) {
        // Enhanced error logging with provider information when available
        if (provider != null) {
            log.error("OAuth2 authentication failed for provider [{}]: {}", provider, error);
        } else {
            log.error("OAuth2 authentication failed: {}", error);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Authentication failed");
        response.put("error", error != null ? error : "Unknown error");

        // Add provider info to response if available
        if (provider != null) {
            response.put("provider", provider);
        }

        return ResponseEntity.status(401).body(response);
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<TokenResponse> success(OAuth2AuthenticationToken authentication) {
        log.info("OAuth2 authentication successful with provider: {}",
                authentication.getAuthorizedClientRegistrationId());
        return ResponseEntity.ok(oauth2Service.processOAuth2Login(authentication));
    }

    /**
     * Handle Facebook OAuth callbacks - Updated with proper error handling
     */
    @GetMapping(path = {
        "/login/oauth2/code/facebook",
        "/oauth2/code/facebook",
        "/oauth2/callback/facebook"
    })
    public void handleFacebookCallback(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            Authentication authentication) throws IOException {

        String fullUri = request.getRequestURI();
        if (request.getQueryString() != null) {
            fullUri += "?" + request.getQueryString();
        }
        log.info("OAuth2 Facebook callback received at {}", fullUri);
        log.info("Authentication object present: {}", authentication != null);
        log.info("Code: {}, State: {}, Error: {}",
                code != null ? "present" : "null", state, error != null ? error : "none");

        String redirectUrl;

        if (error != null) {
            log.error("Facebook OAuth2 error: {}", error);
            redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/auth/oauth2/failure")
                .queryParam("provider", "facebook")
                .queryParam("error", error)
                .build()
                .toUriString();
        } else if (authentication instanceof OAuth2AuthenticationToken) {
            try {
                OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) authentication;
                log.info("Processing OAuth2 authentication for Facebook - User: {}, Provider: {}",
                    oauth2Auth.getName(), oauth2Auth.getAuthorizedClientRegistrationId());

                TokenResponse tokenResponse = oauth2Service.processOAuth2Login(oauth2Auth);
                log.info("Successfully processed user: {}, userId: {}",
                    tokenResponse.getUser().getEmail(),
                    tokenResponse.getUser().getUserId());

                // Determine appropriate parameters
                String provider = oauth2Auth.getAuthorizedClientRegistrationId();
                boolean requiresProfileCompletion = tokenResponse.getRequiresProfileCompletion() != null &&
                    tokenResponse.getRequiresProfileCompletion();
                boolean isNewUser = tokenResponse.getUser().getCreatedAt().equals(tokenResponse.getUser().getUpdatedAt());

                // Set secure JWT cookie
                Cookie jwtCookie = new Cookie("jwt_token", tokenResponse.getToken());
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(3600); // 1 hour
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(request.isSecure());
                response.addCookie(jwtCookie);

                // Store non-sensitive data in cookies
                Cookie providerCookie = new Cookie("auth_provider", provider);
                providerCookie.setPath("/");
                providerCookie.setMaxAge(3600);
                response.addCookie(providerCookie);

                Cookie newUserCookie = new Cookie("is_new_user", String.valueOf(isNewUser));
                newUserCookie.setPath("/");
                newUserCookie.setMaxAge(3600);
                response.addCookie(newUserCookie);

                Cookie completionCookie = new Cookie("requires_completion", String.valueOf(requiresProfileCompletion));
                completionCookie.setPath("/");
                completionCookie.setMaxAge(3600);
                response.addCookie(completionCookie);

                Cookie userIdCookie = new Cookie("user_id", tokenResponse.getUser().getUserId().toString());
                userIdCookie.setPath("/");
                userIdCookie.setMaxAge(3600);
                response.addCookie(userIdCookie);

                // Add welcome message as a Base64 encoded cookie
                String welcomeMessage = createWelcomeMessage(provider, isNewUser, requiresProfileCompletion);
                Cookie messageCookie = new Cookie("welcome_message",
                    Base64.getEncoder().encodeToString(welcomeMessage.getBytes(StandardCharsets.UTF_8)));
                messageCookie.setPath("/");
                messageCookie.setMaxAge(3600);
                response.addCookie(messageCookie);

                // Add email in a Base64 encoded cookie
                Cookie emailCookie = new Cookie("user_email",
                    Base64.getEncoder().encodeToString(tokenResponse.getUser().getEmail().getBytes(StandardCharsets.UTF_8)));
                emailCookie.setPath("/");
                emailCookie.setMaxAge(3600);
                response.addCookie(emailCookie);

                // Redirect user to home page with minimal query parameters
                redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path("/home")
                    .queryParam("auth_success", "true")
                    .queryParam("provider", provider)
                    .build()
                    .toUriString();

                log.info("Facebook OAuth2 authentication successful, redirecting to home page");
            } catch (Exception e) {
                log.error("Error processing Facebook OAuth2 login: {}", e.getMessage(), e);
                redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path("/auth/oauth2/failure")
                    .queryParam("provider", "facebook")
                    .queryParam("error", "processing_error")
                    .queryParam("message", e.getMessage())
                    .build()
                    .toUriString();
            }
        } else {
            log.warn("No OAuth2 authentication found for Facebook callback - this indicates a configuration issue");
            log.warn("You might need to check your OAuth2 client configuration and security settings");

            redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/auth/oauth2/failure")
                .queryParam("provider", "facebook")
                .queryParam("error", "no_authentication")
                .build()
                .toUriString();
        }

        log.info("Redirecting to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    /**
     * Handle Google OAuth callbacks - Updated with proper error handling
     */
    @GetMapping(path = {
        "/login/oauth2/code/google",
        "/oauth2/code/google",
        "/oauth2/callback/google"
    })
    public void handleGoogleCallback(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            Authentication authentication) throws IOException {

        String fullUri = request.getRequestURI();
        if (request.getQueryString() != null) {
            fullUri += "?" + request.getQueryString();
        }
        log.info("OAuth2 Google callback received at {}", fullUri);
        log.info("Authentication object present: {}", authentication != null);
        log.info("Code: {}, State: {}, Error: {}",
                code != null ? "present" : "null", state, error != null ? error : "none");

        String redirectUrl;

        if (error != null) {
            log.error("Google OAuth2 error: {}", error);
            redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/auth/oauth2/failure")
                .queryParam("provider", "google")
                .queryParam("error", error)
                .build()
                .toUriString();
        } else if (authentication instanceof OAuth2AuthenticationToken) {
            try {
                OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) authentication;
                log.info("Processing OAuth2 authentication for Google - User: {}, Provider: {}",
                    oauth2Auth.getName(), oauth2Auth.getAuthorizedClientRegistrationId());

                TokenResponse tokenResponse = oauth2Service.processOAuth2Login(oauth2Auth);
                log.info("Successfully processed user: {}, userId: {}",
                    tokenResponse.getUser().getEmail(),
                    tokenResponse.getUser().getUserId());

                // Determine appropriate parameters
                String provider = oauth2Auth.getAuthorizedClientRegistrationId();
                boolean requiresProfileCompletion = tokenResponse.getRequiresProfileCompletion() != null &&
                    tokenResponse.getRequiresProfileCompletion();
                boolean isNewUser = tokenResponse.getUser().getCreatedAt().equals(tokenResponse.getUser().getUpdatedAt());

                // Set secure JWT cookie
                Cookie jwtCookie = new Cookie("jwt_token", tokenResponse.getToken());
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(3600); // 1 hour
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(request.isSecure());
                response.addCookie(jwtCookie);

                // Store non-sensitive data in cookies
                Cookie providerCookie = new Cookie("auth_provider", provider);
                providerCookie.setPath("/");
                providerCookie.setMaxAge(3600);
                response.addCookie(providerCookie);

                Cookie newUserCookie = new Cookie("is_new_user", String.valueOf(isNewUser));
                newUserCookie.setPath("/");
                newUserCookie.setMaxAge(3600);
                response.addCookie(newUserCookie);

                Cookie completionCookie = new Cookie("requires_completion", String.valueOf(requiresProfileCompletion));
                completionCookie.setPath("/");
                completionCookie.setMaxAge(3600);
                response.addCookie(completionCookie);

                Cookie userIdCookie = new Cookie("user_id", tokenResponse.getUser().getUserId().toString());
                userIdCookie.setPath("/");
                userIdCookie.setMaxAge(3600);
                response.addCookie(userIdCookie);

                // Add welcome message as a Base64 encoded cookie
                String welcomeMessage = createWelcomeMessage(provider, isNewUser, requiresProfileCompletion);
                Cookie messageCookie = new Cookie("welcome_message",
                    Base64.getEncoder().encodeToString(welcomeMessage.getBytes(StandardCharsets.UTF_8)));
                messageCookie.setPath("/");
                messageCookie.setMaxAge(3600);
                response.addCookie(messageCookie);

                // Add email in a Base64 encoded cookie
                Cookie emailCookie = new Cookie("user_email",
                    Base64.getEncoder().encodeToString(tokenResponse.getUser().getEmail().getBytes(StandardCharsets.UTF_8)));
                emailCookie.setPath("/");
                emailCookie.setMaxAge(3600);
                response.addCookie(emailCookie);

                // Redirect user to home page with minimal query parameters
                redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path("/home")
                    .queryParam("auth_success", "true")
                    .queryParam("provider", provider)
                    .build()
                    .toUriString();

                log.info("Google OAuth2 authentication successful, redirecting to home page");
            } catch (Exception e) {
                log.error("Error processing Google OAuth2 login: {}", e.getMessage(), e);
                redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path("/auth/oauth2/failure")
                    .queryParam("provider", "google")
                    .queryParam("error", "processing_error")
                    .queryParam("message", e.getMessage())
                    .build()
                    .toUriString();
            }
        } else {
            log.warn("No OAuth2 authentication found for Google callback - this indicates a configuration issue");
            log.warn("You might need to check your OAuth2 client configuration and security settings");

            redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/auth/oauth2/failure")
                .queryParam("provider", "google")
                .queryParam("error", "no_authentication")
                .build()
                .toUriString();
        }

        log.info("Redirecting to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    /**
     * Creates a personalized welcome message based on authentication provider and user status
     */
    private String createWelcomeMessage(String provider, boolean isNewUser, boolean requiresProfileCompletion) {
        String capitalizedProvider = provider.substring(0, 1).toUpperCase() + provider.substring(1).toLowerCase();

        if (isNewUser) {
            if (requiresProfileCompletion) {
                return "Chào mừng bạn đã đăng ký thành công bằng tài khoản " + capitalizedProvider + "! " +
                       "Vui lòng cập nhật thông tin cá nhân của bạn để hoàn tất đăng ký và được admin xác thực " +
                       "để có thể sử dụng đầy đủ tính năng mua hàng hoặc trở thành người bán.";
            } else {
                return "Chào mừng bạn đã đăng ký thành công bằng tài khoản " + capitalizedProvider + "! " +
                       "Tài khoản của bạn đã sẵn sàng để sử dụng.";
            }
        } else {
            if (requiresProfileCompletion) {
                return "Đăng nhập thành công bằng " + capitalizedProvider + "! " +
                       "Vui lòng cập nhật thông tin cá nhân của bạn để được admin xác thực và sử dụng đầy đủ tính năng.";
            } else {
                return "Đăng nhập thành công bằng " + capitalizedProvider + "!";
            }
        }
    }
}
