package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/facebook/debug")
@RequiredArgsConstructor
@Slf4j
public class FacebookOAuthDebugController {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String facebookRedirectUri;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> debug = new HashMap<>();
        try {
            ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("facebook");
            if (registration != null) {
                debug.put("clientId", "Set (length: " +
                    (registration.getClientId() != null ? registration.getClientId().length() : 0) + ")");
                debug.put("clientSecret", "Set (length: " +
                    (registration.getClientSecret() != null ? registration.getClientSecret().length() : 0) + ")");
                debug.put("registrationId", registration.getRegistrationId());
                debug.put("clientAuthenticationMethod", registration.getClientAuthenticationMethod().getValue());
                debug.put("authorizationGrantType", registration.getAuthorizationGrantType().getValue());
                debug.put("redirectUri", registration.getRedirectUri());
                debug.put("scopes", registration.getScopes());
                debug.put("authorizationUri", registration.getProviderDetails().getAuthorizationUri());
                debug.put("tokenUri", registration.getProviderDetails().getTokenUri());
                debug.put("userInfoUri", registration.getProviderDetails().getUserInfoEndpoint().getUri());
                debug.put("userNameAttributeName",
                     registration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());
                debug.put("configuredRedirectUri", facebookRedirectUri);
            } else {
                debug.put("error", "Facebook client registration not found");
                log.error("Facebook client registration not found in repository");
            }
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            debug.put("error", e.getMessage());
            log.error("Error retrieving Facebook OAuth config", e);
            return ResponseEntity.status(500).body(debug);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Facebook OAuth debug controller is active");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/manual-token")
    public ResponseEntity<Map<String, Object>> getTokenManually(
            @RequestParam String code, @RequestParam(required = false) String state) {

        Map<String, Object> response = new HashMap<>();
        try {
            ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("facebook");

            // Prepare token request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", registration.getClientId());
            formData.add("client_secret", registration.getClientSecret());
            formData.add("code", code);
            formData.add("redirect_uri", registration.getRedirectUri());

            HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(formData, headers);

            // Log the request
            log.debug("Token Request to: {}", registration.getProviderDetails().getTokenUri());
            log.debug("Request headers: {}", headers);
            log.debug("Form data: client_id={}, redirect_uri={}, code length={}",
                     registration.getClientId(), registration.getRedirectUri(), code.length());

            ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                registration.getProviderDetails().getTokenUri(),
                HttpMethod.POST,
                requestEntity,
                Map.class
            );

            // Log and return response
            log.debug("Token Response Status: {}", tokenResponse.getStatusCode());

            Map<String, Object> tokenBody = tokenResponse.getBody();
            response.put("tokenResponse", tokenBody);

            // If token was obtained, try to get user info
            if (tokenBody != null && tokenBody.get("access_token") != null) {
                String accessToken = tokenBody.get("access_token").toString();

                // Now get user info
                String userInfoUri = registration.getProviderDetails().getUserInfoEndpoint().getUri();
                String userInfoUriWithToken = UriComponentsBuilder.fromUriString(userInfoUri)
                    .queryParam("access_token", accessToken)
                    .build().toUriString();

                log.debug("User Info Request to: {}", userInfoUriWithToken);

                ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    userInfoUriWithToken,
                    HttpMethod.GET,
                    null,
                    Map.class
                );

                log.debug("User Info Response Status: {}", userInfoResponse.getStatusCode());
                response.put("userInfoResponse", userInfoResponse.getBody());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in manual token test", e);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/login-url")
    public ResponseEntity<Map<String, String>> getLoginUrl() {
        Map<String, String> response = new HashMap<>();
        try {
            ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("facebook");
            if (registration != null) {
                String authUrl = UriComponentsBuilder.fromUriString(registration.getProviderDetails().getAuthorizationUri())
                    .queryParam("client_id", registration.getClientId())
                    .queryParam("redirect_uri", registration.getRedirectUri())
                    .queryParam("response_type", "code")
                    .queryParam("scope", String.join(",", registration.getScopes()))
                    .build().toUriString();

                response.put("loginUrl", authUrl);
                response.put("note", "Use this URL to initiate Facebook OAuth login. The response will be sent to your redirect URI.");
            } else {
                response.put("error", "Facebook client registration not found");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
