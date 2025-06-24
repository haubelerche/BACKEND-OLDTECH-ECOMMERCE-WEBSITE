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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.Arrays;

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
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final FacebookOAuth2UserService facebookOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/**",
                    "/oauth2/**",
                    "/login/oauth2/code/*",
                    "/public/**",
                    "/oldtech/auth/**",
                    "/oldtech/oauth2/**",
                    "/oldtech/login/oauth2/code/*",
                    "/oldtech/public/**",
                    "/oldtech/facebook/debug/**",
                    "/oldtech/manager/superadmins",
                    "/oldtech/products/**",
                    "/products/**","/oldtech/products/filter/category"
                ).permitAll()


                    .requestMatchers(
                            "/admin/**",
                            "/oldtech/admin/**",
                            "/oldtech/verification/admin/**",
                            "/verification/admin/**",
                            "/oldtech/product/**",
                            "/oldtech/api/**",
                            "/oldtech/admin-alerts/**",  // Removed extra quotation mark
                            "/oldtech/admin-dashboard",
                            "/oldtech/admin-dashboard-etl/**",
                            "/oldtech/etl",
                            "/oldtech/etl/seller-dashboard/**",
                            "/oldtech/etl/seller-dashboard-etl/**",
                            "/oldtech/notifications/**"
                    ).hasAnyAuthority("Admin", "SuperAdmin")

                .requestMatchers( "/oldtech/reviews/**","/oldtech/addresses/**","/oldtech/orders/**", "/oldtech/cart/**"

                            )
                .hasAnyAuthority("Customer")



                    .requestMatchers(
                            "/oldtech/seller/**",
                            "/oldtech/api/**",
                            "/oldtech/api/seller/dashboard",  // Added leading slash
                            "/oldtech/etl",
                            "/oldtech/etl/seller-dashboard/**",
                            "/oldtech/etl/seller-dashboard-etl/**"
                    ).hasAnyAuthority("Seller")


                .requestMatchers(
                        "/oldtech/manager/admins",
                        "/oldtech/manager/**",
                        "/manager/**","/oldtech/**",
                        "/oldtech/manager/admins/**"
                ).hasAuthority("SuperAdmin")


                // TODO: SEARCH FOR PRODUCT LÀ PUBLIC; SEARCH FOR SELLER LÀ CUSTOMER ACTIVITIES; SEARCH FOR ALLS LÀ ADMIN ACTIVITIES
                .requestMatchers(
                    "/oldtech/customer/all",
                    "/oldtech/customer/{userId}",
                        "/oldtech/customer/profile/**",
                    //search
                    "/oldtech/customer/search/name/{name}",
                    "/oldtech/customer/search/email/{email}",
                    "/oldtech/customer/search/phone/{phoneNumber}",
                      "/oldtech/customer/search/email/",
                    "/oldtech/customer/search/phone/",
                    "/oldtech/customer/search/name/"
                ).hasAnyAuthority("Customer", "Admin", "SuperAdmin")
                .anyRequest().authenticated()
            )











            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Changed from ALWAYS to STATELESS for better REST API security
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "https://funny-leading-puma.ngrok-free.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
