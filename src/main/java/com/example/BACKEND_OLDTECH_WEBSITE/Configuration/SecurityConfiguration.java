package com.example.BACKEND_OLDTECH_WEBSITE.Configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.Customizer;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JWTAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - accessible without authentication
                .requestMatchers(
                    "/auth/**",
                    "/oauth2/**",
                    "/public/**",
                    "/oldtech/auth/**",
                    "/oldtech/oauth2/**",
                    "/oldtech/public/**",
                    "/oldtech/manager/superadmins"
                ).permitAll()

                // Admin-only endpoints
                .requestMatchers(
                    "/admin/**",
                    "/oldtech/admin/**",
                    "/oldtech/verification/admin/**"  // Added verification admin endpoint
                ).hasAnyAuthority("Admin", "SuperAdmin")

                // Seller-only endpoints
                .requestMatchers("/seller/**", "/oldtech/seller/**")
                    .hasAnyAuthority("Seller", "Admin", "SuperAdmin")

                // SuperAdmin-only endpoints
                .requestMatchers("/oldtech/manager/admins",
                        "/oldtech/manager/**",
                        "/manager/**",
                        "/oldtech/manager/admins/**"
                ).hasAuthority("SuperAdmin")

                // Customer-specific endpoints that require authentication
                .requestMatchers(
                    "/customer/profile/**",
                    "/customer/{userId}",
                    "/customer/search/{name}",
                    "/customer/**",
                    "/oldtech/customer/**"
                ).hasAnyAuthority("Customer", "Seller", "Admin", "SuperAdmin")

                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

