package com.example.BACKEND_OLDTECH_WEBSITE.Security;

import com.example.BACKEND_OLDTECH_WEBSITE.Service.OAuth2Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2Service oauth2Service;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
            Authentication authentication) throws IOException, ServletException {
        
        if (authentication instanceof OAuth2AuthenticationToken) {
            try {
                oauth2Service.processOAuth2Login((OAuth2AuthenticationToken) authentication);
                String targetUrl = "/oauth2/success";
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            } catch (Exception e) {
                String targetUrl = "/oauth2/failure?error=" + e.getMessage();
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            }
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}