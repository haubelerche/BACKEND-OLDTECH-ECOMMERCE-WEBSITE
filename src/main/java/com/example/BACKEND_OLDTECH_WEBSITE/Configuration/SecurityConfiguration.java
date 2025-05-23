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


                .requestMatchers(
                    "/admin/**",
                    "/oldtech/admin/**",
                    "/oldtech/verification/admin/**"
                ).hasAnyAuthority("Admin", "SuperAdmin")

                //SELLER-ONLY
                .requestMatchers("/seller/**", "/oldtech/seller/**")
                    .hasAnyAuthority("Seller", "Admin", "SuperAdmin")

                //SUPERADMIN-ONLY
                .requestMatchers("/oldtech/manager/admins",
                        "/oldtech/manager/**",
                        "/manager/**",
                        "/oldtech/manager/admins/**"
                ).hasAuthority("SuperAdmin")


                // TODO:CHỈ SEARCH FOR PRODUCT LÀ PUBLIC CÒN SEARCH FOR HUMAN LÀ INTERNAL ACTIVITIES
                .requestMatchers(
                    "/oldtech/customer/all",
                    "/oldtech/customer/{userId}",
                    "/oldtech/customer/search/name/{name}",
                    "/oldtech/customer/search/email/{email}",
                    "/oldtech/customer/search/phone/{phoneNumber}",
                    "/oldtech/customer/profile/**",
                      "/oldtech/customer/search/email/",
                    "/oldtech/customer/search/phone/",
                    "/oldtech/customer/search/name/"
                ).hasAnyAuthority("Customer", "Seller", "Admin", "SuperAdmin")


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


