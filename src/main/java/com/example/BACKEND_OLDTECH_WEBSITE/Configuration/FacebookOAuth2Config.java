package com.example.BACKEND_OLDTECH_WEBSITE.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FacebookOAuth2Config {

    @Value("${app.base-url}")
    private String baseUrl;

    // Hardcoded Facebook OAuth2 credentials for direct use
    private static final String FACEBOOK_CLIENT_ID = "1201867234769030";
    private static final String FACEBOOK_CLIENT_SECRET = "fe6902901b3cd79d54e7fbc0bf41cdc6";

    @Bean
    public ClientRegistration facebookClientRegistration() {
        // Use the standard Spring OAuth2 callback URI pattern
        String redirectUri = baseUrl + "/login/oauth2/code/facebook";
        log.info("Configuring Facebook OAuth2 with redirect URI: {}", redirectUri);

        return ClientRegistration.withRegistrationId("facebook")
            .clientId(FACEBOOK_CLIENT_ID)
            .clientSecret(FACEBOOK_CLIENT_SECRET)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri(redirectUri)
            .scope("email", "public_profile")
            .authorizationUri("https://www.facebook.com/v22.0/dialog/oauth")
            .tokenUri("https://graph.facebook.com/v22.0/oauth/access_token")
            .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,picture.width(250).height(250)")
            .userNameAttributeName("id")
            .clientName("Facebook")
            .build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            facebookClientRegistration()
        );
    }
}
// still heavyly bugging in here, fix later