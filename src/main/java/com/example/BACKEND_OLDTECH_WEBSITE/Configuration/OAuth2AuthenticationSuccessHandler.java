package com.example.BACKEND_OLDTECH_WEBSITE.Configuration;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TokenResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.OAuth2Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2Service oauth2Service;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
            Authentication authentication) throws IOException, ServletException {
        
        if (authentication instanceof OAuth2AuthenticationToken) {
            try {
                TokenResponse tokenResponse = oauth2Service.processOAuth2Login((OAuth2AuthenticationToken) authentication);
                String targetUrl = determineTargetUrl(tokenResponse);
                
                // Set JWT as a cookie
                Cookie jwtCookie = new Cookie("jwt_token", tokenResponse.getToken());
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(3600); // 1 hour
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(request.isSecure());
                response.addCookie(jwtCookie);
                
                // Also send user info in a safe way
                String userJson = objectMapper.writeValueAsString(tokenResponse.getUser());
                String encodedUserInfo = Base64.getEncoder().encodeToString(userJson.getBytes(StandardCharsets.UTF_8));
                Cookie userInfoCookie = new Cookie("user_info", encodedUserInfo);
                userInfoCookie.setPath("/");
                userInfoCookie.setMaxAge(3600);
                response.addCookie(userInfoCookie);
                
                // Set a registration status cookie for frontend to detect
                boolean isNewRegistration = tokenResponse.getUser().getPhoneNumber() == null
                    || tokenResponse.getUser().getDob() == null;
                String registrationStatus = isNewRegistration ? "incomplete" : "complete";

                Cookie registrationStatusCookie = new Cookie("registration_status", registrationStatus);
                registrationStatusCookie.setPath("/");
                registrationStatusCookie.setMaxAge(3600);
                response.addCookie(registrationStatusCookie);

                // Add user ID in a separate cookie for easy access
                Cookie userIdCookie = new Cookie("user_id", tokenResponse.getUser().getUserId().toString());
                userIdCookie.setPath("/");
                userIdCookie.setMaxAge(3600);
                response.addCookie(userIdCookie);

                log.info("OAuth2 authentication successful for user: {}, redirecting to: {}",
                        tokenResponse.getUser().getEmail(), targetUrl);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            } catch (Exception e) {
                log.error("OAuth2 authentication error: {}", e.getMessage());
                String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                    .queryParam("error", "Authentication failed: " + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8))
                    .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
            }
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
    
    private String determineTargetUrl(TokenResponse tokenResponse) {
        // Check if profile is incomplete but allow login anyway
        boolean isProfileIncomplete = tokenResponse.getUser().getPhoneNumber() == null ||
                tokenResponse.getUser().getDob() == null;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(frontendUrl);
        String targetUrl;
        
        if (isProfileIncomplete) {
            // Redirect to dashboard with a notification flag to show profile completion reminder
            targetUrl = builder.path("/dashboard")
                .queryParam("isProfileIncomplete", "true")
                .queryParam("userId", tokenResponse.getUser().getUserId())
                .queryParam("email", URLEncoder.encode(tokenResponse.getUser().getEmail(), StandardCharsets.UTF_8))
                .build().toUriString();
        } else {
            // Redirect to dashboard without notification
            targetUrl = builder.path("/dashboard")
                .build().toUriString();
        }
        
        // Validate the redirect URL before returning it
        if (!isValidRedirectUrl(targetUrl)) {
            log.warn("Invalid redirect URL detected: {}, defaulting to frontend URL", targetUrl);
            return frontendUrl;
        }
        
        return targetUrl;
    }
    
    private boolean isValidRedirectUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return host != null && (host.contains("localhost") || host.contains("oldtech.com"));
        } catch (Exception e) {
            return false;
        }
    }
}

