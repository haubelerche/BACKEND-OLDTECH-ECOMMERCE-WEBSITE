package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth2/debug")
@RequiredArgsConstructor
@Slf4j
public class OAuth2DebugController {

    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/facebook-config")
    public ResponseEntity<Map<String, Object>> getFacebookConfig() {
        log.info("Retrieving Facebook OAuth2 configuration for debugging");
        Map<String, Object> config = new HashMap<>();

        try {
            ClientRegistration facebookRegistration = clientRegistrationRepository.findByRegistrationId("facebook");

            if (facebookRegistration != null) {
                config.put("clientId", "Set (length: " +
                    (facebookRegistration.getClientId() != null ? facebookRegistration.getClientId().length() : 0) + ")");
                config.put("clientSecret", "Set (length: " +
                    (facebookRegistration.getClientSecret() != null ? facebookRegistration.getClientSecret().length() : 0) + ")");
                config.put("redirectUri", facebookRegistration.getRedirectUri());
                config.put("authorizationUri", facebookRegistration.getProviderDetails().getAuthorizationUri());
                config.put("tokenUri", facebookRegistration.getProviderDetails().getTokenUri());
                config.put("userInfoUri", facebookRegistration.getProviderDetails().getUserInfoEndpoint().getUri());
                config.put("scopes", facebookRegistration.getScopes());
                config.put("registrationId", facebookRegistration.getRegistrationId());
                config.put("clientAuthenticationMethod", facebookRegistration.getClientAuthenticationMethod().getValue());
                config.put("authorizationGrantType", facebookRegistration.getAuthorizationGrantType().getValue());
            } else {
                config.put("error", "Facebook client registration not found");
                log.error("Facebook client registration not found in repository");
            }

            // Add frontend and backend URLs for verification
            config.put("frontendUrl", frontendUrl);
            config.put("baseUrl", baseUrl);
            config.put("loginUrl", baseUrl + "/oauth2/authorization/facebook");

            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error retrieving Facebook OAuth2 configuration", e);
            config.put("error", e.getMessage());
            return ResponseEntity.status(500).body(config);
        }
    }

    @GetMapping("/callback-test")
    public ResponseEntity<Map<String, String>> testCallback() {
        log.info("OAuth2 callback test endpoint accessed");
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "If you see this, your OAuth2 callback URL is correctly configured to reach the server");
        return ResponseEntity.ok(response);
    }
}
