package com.example.BACKEND_OLDTECH_WEBSITE.Configuration;

import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            logger.debug("Attempting to find user with email: {}", email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("User not found with email: {}", email);
                        return new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email);
                    });


            String authority = user.getRole().name();

            logger.info("UserDetailsService - User: {}, DB Role: {}, Granted Authority: {}",
                    user.getEmail(), user.getRole().name(), authority);

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(authority))
            );
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

