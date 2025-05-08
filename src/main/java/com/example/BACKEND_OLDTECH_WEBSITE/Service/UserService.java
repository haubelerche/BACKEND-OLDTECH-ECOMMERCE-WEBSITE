package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AuthProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.UUID;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Transactional
    public User createUser(RegisterRequest registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setCreatedAt(Timestamp.from(Instant.now()));
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        user.setIsActive(true);
        user.setRole(RoleEnum.USER);
        user.setAuthProvider(AuthProvider.valueOf("local"));

        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return (UserDetails) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    @Transactional
    public User processOAuth2User(OAuth2User oauth2User, String provider) {
        String email = oauth2User.getAttribute("email");
        String providerId = oauth2User.getAttribute("sub");
        
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                // Split the name into first and last name
                String fullName = oauth2User.getAttribute("name");
                String[] nameParts = fullName.split(" ", 2);
                newUser.setFirstName(nameParts[0]);
                newUser.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                newUser.setAuthProvider(AuthProvider.valueOf(provider));
                newUser.setAuthProviderId(providerId);
                newUser.setAuthProviderToken(oauth2User.getAttribute("access_token"));
                newUser.setAuthProviderRefreshToken(oauth2User.getAttribute("refresh_token"));
                newUser.setAuthProviderTokenExpires(Timestamp.from(Instant.now().plusSeconds(3600))); // Default 1 hour
                newUser.setIsActive(true);
                newUser.setRole(RoleEnum.USER);
                newUser.setCreatedAt(Timestamp.from(Instant.now()));
                newUser.setUpdatedAt(Timestamp.from(Instant.now()));
                return newUser;
            });

        // Update OAuth2 tokens for existing user
        user.setAuthProviderToken(oauth2User.getAttribute("access_token"));
        user.setAuthProviderRefreshToken(oauth2User.getAttribute("refresh_token"));
        user.setAuthProviderTokenExpires(Timestamp.from(Instant.now().plusSeconds(3600)));
        user.setLastLogin(Timestamp.from(Instant.now()));
        
        return userRepository.save(user);
    }
}
