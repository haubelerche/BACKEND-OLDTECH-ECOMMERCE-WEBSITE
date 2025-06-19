package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom implementation for Facebook OAuth2 user service with enhanced logging and error handling.
 */
@Slf4j
@Component
public class FacebookOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String FACEBOOK_PROVIDER_ID = "facebook";
    private static final String GRAPH_API_VERSION = "v18.0"; // Updated to a supported API version
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${FACEBOOK_CLIENT_SECRET}")
    private String clientSecret;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String providerId = userRequest.getClientRegistration().getRegistrationId();

        log.info("Processing OAuth2 user request for provider: {}", providerId);

        if (FACEBOOK_PROVIDER_ID.equals(providerId)) {
            try {
                // Get direct response from Facebook using current API version
                String accessToken = userRequest.getAccessToken().getTokenValue();

                // Generate appsecret_proof - required by Facebook for server-side API calls
                String appsecretProof = generateAppSecretProof(accessToken, clientSecret);

                // Build a URI using correct API version with appsecret_proof
                URI uri = UriComponentsBuilder
                    .fromUriString("https://graph.facebook.com/" + GRAPH_API_VERSION + "/me")
                    .queryParam("fields", "id,name,email,picture")
                    .queryParam("access_token", accessToken)
                    .queryParam("appsecret_proof", appsecretProof)
                    .build()
                    .toUri();

                log.debug("Requesting user info from Facebook Graph API: {}", uri);

                // Create a new RestTemplate that won't throw exceptions on 4xx
                RestTemplate restTemplate = new RestTemplate();

                // Get raw response from Facebook
                ResponseEntity<String> rawResponse = restTemplate.exchange(
                    RequestEntity.get(uri).build(),
                    String.class
                );

                log.debug("Facebook Graph API response: {}", rawResponse.getBody());

                // Parse the response body into a Map
                Map<String, Object> attributes;
                try {
                    attributes = objectMapper.readValue(rawResponse.getBody(), Map.class);
                } catch (Exception e) {
                    log.error("Error parsing Facebook user info JSON: {}", e.getMessage());
                    throw new RuntimeException("Error parsing Facebook user info", e);
                }

                // Handle email if missing
                if (!attributes.containsKey("email") || attributes.get("email") == null) {
                    log.warn("Facebook user is missing email attribute, generating a placeholder");
                    String facebookId = attributes.get("id").toString();
                    attributes.put("email", "facebook_" + facebookId + "@placeholder.com");
                }

                // Handle nested picture object
                if (attributes.containsKey("picture") && attributes.get("picture") instanceof Map) {
                    Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
                    if (pictureObj.containsKey("data") && pictureObj.get("data") instanceof Map) {
                        Map<String, Object> pictureData = (Map<String, Object>) pictureObj.get("data");
                        if (pictureData.containsKey("url")) {
                            // Flatten the picture URL to make it easier to access
                            attributes.put("picture_url", pictureData.get("url"));
                        }
                    }
                }

                return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "id" // Facebook uses "id" as the name attribute key
                );
            } catch (Exception ex) {
                log.error("Error loading Facebook user: {}", ex.getMessage());
                // Log full stack trace at debug level
                log.debug("Exception details", ex);

                OAuth2Error oauth2Error = new OAuth2Error(
                    "facebook_user_info_error",
                    "Error processing Facebook user info: " + ex.getMessage(),
                    null
                );
                throw new OAuth2AuthenticationException(oauth2Error, ex);
            }
        }

        // For non-Facebook providers, use the default implementation
        return delegate.loadUser(userRequest);
    }

    /**
     * Generates the appsecret_proof parameter required by Facebook for server-side API calls.
     * This is a SHA-256 HMAC of the access token, using the app secret as the key.
     */
    private String generateAppSecretProof(String accessToken, String appSecret) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(appSecret.getBytes(), "HmacSHA256");
            hmacSha256.init(secretKey);
            byte[] hmacData = hmacSha256.doFinal(accessToken.getBytes());

            // Convert to hex string
            StringBuilder result = new StringBuilder();
            for (byte b : hmacData) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to generate Facebook appsecret_proof: {}", e.getMessage());
            throw new RuntimeException("Could not generate appsecret_proof", e);
        }
    }
}
