package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepo userRepo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String googleId = oauth2User.getAttribute("sub");

        User user = userRepo.findByEmail(email);
        if (user == null) {
            user = userRepo.findByPhoneNumber(email); // fallback
        }

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setUsername(generateUsername(email));
            user.setPassword("oauth2_login"); // placeholder
            user.setPhoneNumber(email); // or a default value
            user.setFirstName(name != null ? name.split(" ")[0] : "OAuth");
            user.setLastName(name != null && name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "User");
            user.setRole(RoleEnum.CUSTOMER);
            user.setRefundMomoAccount("0000000000");
            user.setAccountStatus(AccountStatusEnum.ACTIVE);
            Timestamp now = Timestamp.from(Instant.now());
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            user.setLastLogin(now);

            userRepo.save(user);
        } else {
            user.setLastLogin(Timestamp.from(Instant.now()));
            userRepo.save(user);
        }

        return oauth2User;
    }

    private String generateUsername(String email) {
        String base = email.split("@")[0];
        String username = base;
        int suffix = 1;
        while (userRepo.existsByUsername(username)) {
            username = base + suffix;
            suffix++;
        }
        return username;
    }
}
