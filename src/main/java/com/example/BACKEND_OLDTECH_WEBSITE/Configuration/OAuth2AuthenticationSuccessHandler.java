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
                
                // Also send user info back
                String userJson = objectMapper.writeValueAsString(tokenResponse.getUser());
                Cookie userInfoCookie = new Cookie("user_info", userJson);
                userInfoCookie.setPath("/");
                userInfoCookie.setMaxAge(3600);
                response.addCookie(userInfoCookie);
                
                log.info("OAuth2 authentication successful, redirecting to: {}", targetUrl);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            } catch (Exception e) {
                log.error("OAuth2 authentication error: {}", e.getMessage());
                String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                    .queryParam("error", "Authentication failed: " + e.getMessage())
                    .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
            }
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
    
    private String determineTargetUrl(TokenResponse tokenResponse) {
        boolean isProfileComplete = tokenResponse.getUser().getPhoneNumber() != null &&
                tokenResponse.getUser().getDob() != null;
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(frontendUrl);
        String targetUrl;
        
        if (!isProfileComplete) {
            // Redirect to complete profile page
            targetUrl = builder.path("/complete-profile")
                .queryParam("userId", tokenResponse.getUser().getUserId())
                .build().toUriString();
        } else {
            // Redirect to home page or dashboard
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