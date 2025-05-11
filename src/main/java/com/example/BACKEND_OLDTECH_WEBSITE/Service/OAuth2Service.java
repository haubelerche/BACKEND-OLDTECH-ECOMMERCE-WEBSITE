package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2UserInfo;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TokenResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AuthProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Notification;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2Service {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final NotificationRepository notificationRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Transactional
    public TokenResponse processOAuth2Login(OAuth2AuthenticationToken authentication) {
        OAuth2User oauth2User = authentication.getPrincipal();
        String providerName = authentication.getAuthorizedClientRegistrationId();
        
        // Get token information from the authorized client
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
            providerName, authentication.getName());
        
        // Extract token details
        Map<String, Object> tokenAttributes = new HashMap<>();
        if (client != null) {
            OAuth2AccessToken accessToken = client.getAccessToken();
            OAuth2RefreshToken refreshToken = client.getRefreshToken();
            
            if (accessToken != null) {
                tokenAttributes.put("access_token", accessToken.getTokenValue());
                tokenAttributes.put("token_expires", accessToken.getExpiresAt() != null ? 
                    accessToken.getExpiresAt().toEpochMilli() : null);
            }
            
            if (refreshToken != null) {
                tokenAttributes.put("refresh_token", refreshToken.getTokenValue());
            }
        }
        
        // Merge token information with user attributes
        Map<String, Object> userAttributes = new HashMap<>(oauth2User.getAttributes());
        userAttributes.putAll(tokenAttributes);
        
        // Convert to OAuth2User with token information
        OAuth2User enhancedUser = new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
            oauth2User.getAuthorities(),
            userAttributes,
            "name"
        );
        
        log.info("Processing OAuth2 login for provider: {}", providerName);
        User user = userService.processOAuth2User(enhancedUser, providerName);
        
        // Check if this is a first-time login or a returning user with incomplete profile
        boolean isNewUser = user.getCreatedAt().equals(user.getUpdatedAt());
        boolean isProfileIncomplete = isProfileIncomplete(user);
        
        if (isNewUser) {
            log.info("New user registered via OAuth2: {}", user.getEmail());
            sendWelcomeNotification(user);
            sendProfileCompletionNotification(user);
            sendVerificationRequiredNotification(user);
        } else if (isProfileIncomplete) {
            log.info("Profile is incomplete for user: {}", user.getEmail());
            sendProfileCompletionNotification(user);
        }
        
        // Add information to token response
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("profileComplete", !isProfileIncomplete);
        additionalInfo.put("isNewUser", isNewUser);
        additionalInfo.put("authProvider", providerName);
        
        String enhancedToken = jwtService.generateToken(additionalInfo, user);
        
        return new TokenResponse(enhancedToken, user);
    }

    private void sendWelcomeNotification(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Chào mừng bạn đến với OldTech! Cảm ơn bạn đã đăng ký tài khoản.");
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notification.setRead(false);
        notification.setNotificationType(NotificationTypeEnum.System);
        notificationRepository.save(notification);
        
        log.info("Sent welcome notification to user ID: {}", user.getUserId());
    }

    private boolean isProfileIncomplete(User user) {
        // Check required fields for a complete profile
        return user.getPhoneNumber() == null || 
               user.getDob() == null ||
               (!user.getIsVerified() && 
                (user.getRole() == RoleEnum.Seller || user.getRole() == RoleEnum.Customer));
    }

    private void sendProfileCompletionNotification(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Vui lòng vào phần cài đặt và cập nhật đầy đủ thông tin cá nhân của bạn để hoàn tất đăng ký tài khoản.");
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notification.setRead(false);
        notification.setNotificationType(NotificationTypeEnum.System);
        notificationRepository.save(notification);
        
        log.info("Sent profile completion notification to user ID: {}", user.getUserId());
    }
    
    private void sendVerificationRequiredNotification(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Vui lòng gửi tất cả thông tin cần thiết để xác minh tài khoản của bạn. Điều này sẽ giúp tăng độ tin cậy khi sử dụng nền tảng của chúng tôi.");
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notification.setRead(false);
        notification.setNotificationType(NotificationTypeEnum.System);
        notificationRepository.save(notification);
        
        log.info("Sent verification required notification to user ID: {}", user.getUserId());
    }

    @Transactional
    public TokenResponse completeOAuth2Registration(Integer userId, OAuth2RegisterRequest request) {
        User user = userService.getUserById(userId);
        
        // Validate that this is an OAuth2 user
        if (user.getAuthProvider() == null || user.getAuthProvider() == AuthProvider.local) {
            throw new IllegalArgumentException("Người dùng này không đăng ký qua OAuth2.");
        }
        
        // Update user with missing information
        if (request.getPhoneNumber() != null) {
            // Check if phone number is unique
            if (userService.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng.");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }
        
        if (request.getDob() != null) {
            user.setDob(new java.sql.Date(request.getDob().getTime()));
        }
        
        if (request.getRefundMomoAccount() != null) {
            user.setRefundMomoAccount(request.getRefundMomoAccount());
        }
        
        // If the user requested to be a seller, update role
        if (Boolean.TRUE.equals(request.getIsSellerRequest()) && user.getRole() == RoleEnum.Customer) {
            user.setRole(RoleEnum.Seller);
            // Send notification to admin for approval
            sendSellerRequestNotification(user);
        }
        
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        User updatedUser = userService.updateUserForOAuth2(user);
        
        log.info("Completed OAuth2 registration for user ID: {}", userId);
        
        // Generate new token with updated information
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("profileComplete", !isProfileIncomplete(updatedUser));
        additionalInfo.put("authProvider", updatedUser.getAuthProvider().name());
        
        String token = jwtService.generateToken(additionalInfo, updatedUser);
        
        return new TokenResponse(token, updatedUser);
    }

    private void sendSellerRequestNotification(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Yêu cầu xác thực tài khoản người bán của bạn đã được gửi. Vui lòng chờ quản trị viên phê duyệt, chúng tôi sẽ thông báo cho bạn sớm.");
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notification.setRead(false);
        notification.setNotificationType(NotificationTypeEnum.AdminMessage);
        notificationRepository.save(notification);
        
        log.info("Sent seller verification request notification to user ID: {}", user.getUserId());
        
        // Send a notification to admin users
        sendAdminNotificationForSellerRequest(user);
    }
    
    private void sendAdminNotificationForSellerRequest(User seller) {
        // Here you would create a notification for admin users
        // This is a placeholder for actual implementation
        log.info("Admin notification: New seller verification request from user ID: {}", seller.getUserId());
    }

    public OAuth2UserInfo getCurrentOAuth2UserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            return new OAuth2UserInfo(
                oauth2User.getAttribute("name"),
                oauth2User.getAttribute("email"),
                oauth2User.getAttribute("picture")
            );
        }
        return null;
    }
}