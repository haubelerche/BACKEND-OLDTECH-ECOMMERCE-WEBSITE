package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TokenResponse;
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
        
        log.info("OAuth2AuthenticationSuccessHandler: Processing OAuth2 authentication success");
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Authentication class: {}", authentication != null ? authentication.getClass().getName() : "null");
        log.info("Authentication provider: {}", authentication instanceof OAuth2AuthenticationToken ?
                ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId() : "unknown");

        if (authentication instanceof OAuth2AuthenticationToken) {
            try {
                OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) authentication;
                log.info("Processing OAuth2 authentication for provider: {}",
                    oauth2Auth.getAuthorizedClientRegistrationId());

                TokenResponse tokenResponse = oauth2Service.processOAuth2Login(oauth2Auth);
                log.info("Successfully processed user: {}, ID: {}",
                    tokenResponse.getUser().getEmail(),
                    tokenResponse.getUser().getUserId());

                // Determine appropriate redirect URL
                String targetUrl;
                String provider = oauth2Auth.getAuthorizedClientRegistrationId();
                boolean requiresProfileCompletion = tokenResponse.getRequiresProfileCompletion() != null &&
                    tokenResponse.getRequiresProfileCompletion();
                boolean isNewUser = tokenResponse.getUser().getCreatedAt().equals(tokenResponse.getUser().getUpdatedAt());

                // Create secure, HTTP-only cookie for JWT token
                Cookie jwtCookie = new Cookie("jwt_token", tokenResponse.getToken());
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(3600); // 1 hour
                jwtCookie.setHttpOnly(true); // Makes it inaccessible to JavaScript
                jwtCookie.setSecure(request.isSecure()); // Only sent over HTTPS if request is secure
                response.addCookie(jwtCookie);

                // Redirect to home page with minimal query parameters (no token)
                targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path("/home")
                    .queryParam("auth_success", "true")
                    .queryParam("provider", provider)
                    .build().toUriString();

                log.info("User authenticated with {} OAuth2, redirecting to home page with secure cookies", provider);

                // Store non-sensitive data in regular cookies
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

                Cookie messageCookie = new Cookie("welcome_message",
                    Base64.getEncoder().encodeToString(
                        createWelcomeMessage(provider, isNewUser, requiresProfileCompletion).getBytes(StandardCharsets.UTF_8)));
                messageCookie.setPath("/");
                messageCookie.setMaxAge(3600);
                response.addCookie(messageCookie);

                // Add email in a cookie - encode it as it may contain special characters
                Cookie emailCookie = new Cookie("user_email",
                    Base64.getEncoder().encodeToString(tokenResponse.getUser().getEmail().getBytes(StandardCharsets.UTF_8)));
                emailCookie.setPath("/");
                emailCookie.setMaxAge(3600);
                response.addCookie(emailCookie);

                log.info("OAuth2 authentication successful for user: {}, redirecting to: {}",
                        tokenResponse.getUser().getEmail(), targetUrl);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            } catch (Exception e) {
                log.error("OAuth2 authentication error: {}", e.getMessage(), e);
                String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path("/auth/oauth2/failure")
                    .queryParam("provider", ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId())
                    .queryParam("error", "processing_error")
                    .queryParam("message", URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8))
                    .build().toUriString();

                log.info("Redirecting to error page: {}", errorUrl);
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
            }
        } else {
            log.warn("Authentication is not OAuth2AuthenticationToken: {}",
                authentication != null ? authentication.getClass().getName() : "null");
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    private boolean isValidRedirectUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return host != null && (host.contains("localhost") || host.contains("oldtech.com"));
        } catch (Exception e) {
            log.error("Error validating redirect URL: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Creates a personalized welcome message based on authentication provider and user status
     */
    private String createWelcomeMessage(String provider, boolean isNewUser, boolean requiresProfileCompletion) {
        String capitalizedProvider = provider.substring(0, 1).toUpperCase() + provider.substring(1).toLowerCase();

        if (isNewUser) {
            if (requiresProfileCompletion) {
                return "Chào mừng bạn đã đăng ký thành công bằng tài khoản " + capitalizedProvider + "! " +
                       "Vui lòng cập nhật thông tin cá nhân của bạn để hoàn tất đăng ký và được sử dụng đầy đủ chức năng " +
                       "để có thể sử dụng đầy đủ tính năng mua hàng hoặc trở thành người bán.";
            } else {
                return "Chào mừng bạn đã đăng ký thành công bằng tài khoản " + capitalizedProvider + "! " +
                       "Tài khoản của bạn đã sẵn sàng để sử dụng.";
            }
        } else {
            if (requiresProfileCompletion) {
                return "Đăng nhập thành công bằng " + capitalizedProvider + "! " +
                       "Vui lòng cập nhật thông tin cá nhân của bạn để được xác thực và sử dụng đầy đủ tính năng.";
            } else {
                return "Đăng nhập thành công bằng " + capitalizedProvider + "!";
            }
        }
    }
}
