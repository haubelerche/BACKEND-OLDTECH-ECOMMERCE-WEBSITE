package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2UserInfo;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TokenResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2Service {
    
    private final UserService userService;
    private final JwtService jwtService;

    public TokenResponse processOAuth2Login(OAuth2AuthenticationToken authentication) {
        OAuth2User oauth2User = authentication.getPrincipal();
        String provider = authentication.getAuthorizedClientRegistrationId();
        
        User user = userService.processOAuth2User(oauth2User, provider);
        String token = jwtService.generateToken(user);
        
        return new TokenResponse(token, user);
    }

    public OAuth2UserInfo getCurrentOAuth2UserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            return new OAuth2UserInfo(
                oauth2User.getAttribute("name"),
                oauth2User.getAttribute("email"),
                oauth2User.getAttribute("picture")
            );
        }
        return null;
    }
}