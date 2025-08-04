package com.example.BACKEND_OLDTECH_WEBSITE.Configuration;

import com.example.BACKEND_OLDTECH_WEBSITE.Service.FacebookOAuth2UserService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JWTAuthenticationFilter jwtAuthFilter;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final FacebookOAuth2UserService facebookOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Đảm bảo dùng đúng bean CORS, không phụ thuộc vào withDefaults
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    // Only allow /auth endpoints except /auth/profile
                    "/auth/login", "/auth/register",
                    "/auth/2fa/**",
                    "/oauth2/**",
                    "/login/oauth2/code/*",
                    "/public/**",
                    
                    "/oauth2/**",
                    "/login/oauth2/code/*",
                    "/public/**",
                    "/facebook/debug/**",
                    "/manager/superadmins",
                    "/products/**",
                    "/products/filter/category",
                    "/api/seller-dashboard/**",
                    "/admin-dashboard/**",
                    "/health/**"
                ).permitAll()
                .requestMatchers("/auth/profile").authenticated()
              
                .requestMatchers(
                    "/admin/**",
                    "/oldtech/admin/**",
                    "/oldtech/verification/admin/**",
                    "/verification/admin/**",
                    "/oldtech/product/**",
                    "/oldtech/api/**",
                    "/oldtech/admin-alerts/**",
                    "/oldtech/admin-dashboard",
                    "/oldtech/admin-dashboard-etl/**",
                    "/oldtech/etl",
                    "/oldtech/etl/seller-dashboard/**",
                    "/oldtech/etl/seller-dashboard-etl/**",
                    "/oldtech/notifications/**"
                ).hasAnyAuthority("Admin", "SuperAdmin")
                .requestMatchers(
                    "/oldtech/reviews/**",
                    "/oldtech/addresses/**",
                    "/oldtech/orders/**",
                    "/oldtech/cart/**"
                ).permitAll()
                .requestMatchers(
                    "/oldtech/seller/**").permitAll()
                .requestMatchers(
                    "/oldtech/seller/**",
                    "/oldtech/api/**",
                    "/oldtech/api/seller/dashboard",
                    "/oldtech/etl",
                    "/oldtech/etl/seller-dashboard/**",
                    "/oldtech/etl/seller-dashboard-etl/**"
                ).hasAnyAuthority("Seller")
                .requestMatchers(
                    "/oldtech/manager/admins",
                    "/oldtech/manager/**",
                    "/manager/**",
                    "/oldtech/**",
                    "/oldtech/manager/admins/**"
                ).hasAuthority("SuperAdmin")
                .requestMatchers(
                    "/oldtech/customer/single/**").permitAll()
                .requestMatchers(
                    "/oldtech/customer/all",
                    "/oldtech/customer/{userId}",
                    "/oldtech/customer/profile/**",
                    "/oldtech/customer/single",
                    //search
                    "/oldtech/customer/search/name/{name}",
                    "/oldtech/customer/search/email/{email}",
                    "/oldtech/customer/search/phone/{phoneNumber}",
                    "/oldtech/customer/search/email/",
                    "/oldtech/customer/search/phone/",
                    "/oldtech/customer/search/name/"
                ).hasAnyAuthority("Customer", "Admin", "SuperAdmin")
                .requestMatchers("/oldtech/sellers/**").permitAll()
                .requestMatchers("/oldtech/customer/single/**").permitAll()
                // Tất cả các request còn lại (bao gồm /auth/profile) chỉ cần authenticated
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(auth -> auth
                    .baseUri("/oauth2/authorization"))
                .redirectionEndpoint(redirect -> redirect
                    .baseUri("/login/oauth2/code/*"))
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(facebookOAuth2UserService))
                .successHandler(oauth2AuthenticationSuccessHandler)
                .failureUrl("/oauth2/failure?error=Authentication+failed")
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://funny-leading-puma.ngrok-free.app"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
