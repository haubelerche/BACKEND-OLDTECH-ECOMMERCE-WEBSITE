package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2UserInfo;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TokenResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.OAuth2RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AuthProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Exception.AccountSuspendedException;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Notification;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.NotificationRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.JwtService;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
        String providerName = authentication.getAuthorizedClientRegistrationId().toLowerCase();

        log.info("OAuth2 login attempt with provider: {}", providerName);
        log.info("OAuth2 attributes available: {}", oauth2User.getAttributes().keySet());

        // Extract user attributes using provider-specific methods
        String email = extractEmail(oauth2User, providerName);
        String name = extractName(oauth2User, providerName);
        String firstName = extractFirstName(oauth2User, providerName);
        String lastName = extractLastName(oauth2User, providerName);
        String pictureUrl = extractPictureUrl(oauth2User, providerName);
        String providerId = extractProviderId(oauth2User, providerName);

        log.info("Extracted OAuth2 attributes - Provider: {}, Email: {}, Name: {}, Picture: {} available",
            providerName, email, name, pictureUrl != null);

        // Add extracted attributes to the user's attributes
        Map<String, Object> enhancedAttributes = new HashMap<>(oauth2User.getAttributes());

        // Ensure we have critical attributes
        if (email == null) {
            email = generatePlaceholderEmail(providerName, providerId);
            enhancedAttributes.put("email", email);
            log.info("Generated placeholder email: {}", email);
        }

        if (name == null) {
            name = generateName(firstName, lastName, providerId);
            enhancedAttributes.put("name", name);
            log.info("Generated name from components: {}", name);
        }

        // Get token information from the authorized client
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
            authentication.getAuthorizedClientRegistrationId(), authentication.getName());

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
        enhancedAttributes.putAll(tokenAttributes);

        // Convert to OAuth2User with token information
        String nameAttributeKey = "name";

        // Use a fallback if name is missing
        if (!enhancedAttributes.containsKey(nameAttributeKey)) {
            nameAttributeKey = getNameAttributeKeyForProvider(providerName);
            log.info("Using alternate name attribute key for {}: {}", providerName, nameAttributeKey);
        }

        OAuth2User enhancedUser = new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
            oauth2User.getAuthorities(),
            enhancedAttributes,
            nameAttributeKey
        );
        
        // Process the OAuth2 user, which will either find an existing user or create a new one
        User user = userService.processOAuth2User(enhancedUser, providerName);
        log.info("OAuth2 user processed: {}, ID: {}", user.getEmail(), user.getUserId());

        // Check if account is suspended
        checkAccountStatus(user);

        // Update last login time
        user.setLastLogin(new java.sql.Timestamp(System.currentTimeMillis()));
        userService.updateUserForOAuth2(user);
        log.info("User last login updated");

        // Check if this is a first-time login or a returning user with incomplete profile
        boolean isNewUser = user.getCreatedAt().equals(user.getUpdatedAt());
        boolean isProfileIncomplete = isProfileIncomplete(user);
        boolean requiresProfileCompletion = isProfileIncomplete ||
            (user.getPhoneNumber() != null && user.getPhoneNumber().startsWith("placeholder_"));

        log.info("OAuth2 login: User {} - isNewUser: {}, isProfileIncomplete: {}, provider: {}",
               user.getEmail(), isNewUser, isProfileIncomplete, providerName);

        if (isNewUser) {
            log.info("New user registered via OAuth2: {}", user.getEmail());
            sendWelcomeNotification(user);

            if (requiresProfileCompletion) {
                sendProfileCompletionNotification(user);
            }
        }
        
        // Generate JWT token with proper claims
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("requiresProfileCompletion", requiresProfileCompletion);
        additionalClaims.put("authProvider", user.getAuthProvider().name().toLowerCase());
        additionalClaims.put("isNewUser", isNewUser);

        String jwt = jwtService.generateToken(additionalClaims, user);

        log.info("JWT token generated for user: {}", user.getEmail());

        // Return token response with user info and registration status
        return TokenResponse.builder()
                .token(jwt)
                .user(user)
                .expiresIn(3600) // 1 hour in seconds
                .tokenType("Bearer")
                .requiresProfileCompletion(requiresProfileCompletion)
                .build();
    }

    /**
     * Extract email from OAuth2User attributes based on provider
     */
    private String extractEmail(OAuth2User oauth2User, String provider) {
        switch (provider) {
            case "google":
                return oauth2User.getAttribute("email");
            case "facebook":
                String email = oauth2User.getAttribute("email");
                if (email == null || email.isEmpty()) {
                    // Log the missing email for debugging
                    log.warn("Facebook did not return email. Available attributes: {}",
                        oauth2User.getAttributes().keySet());

                    // Try alternate ways of accessing email
                    Map<String, Object> jsonMap = oauth2User.getAttribute("_json");
                    if (jsonMap != null && jsonMap.containsKey("email")) {
                        email = (String) jsonMap.get("email");
                        log.info("Retrieved email from _json property: {}", email);
                    }

                    // If still no email, create a placeholder with the Facebook ID
                    if (email == null || email.isEmpty()) {
                        String id = oauth2User.getAttribute("id");
                        email = id != null ? "facebook_" + id + "@placeholder.com" : null;
                        log.info("Using placeholder email: {}", email);
                    }
                }
                return email;
            default:
                return oauth2User.getAttribute("email");
        }
    }

    /**
     * Extract full name from OAuth2User attributes based on provider
     */
    private String extractName(OAuth2User oauth2User, String provider) {
        switch (provider) {
            case "google":
                return oauth2User.getAttribute("name");
            case "facebook":
                return oauth2User.getAttribute("name");
            default:
                // Try to construct from first and last name
                String firstName = oauth2User.getAttribute("first_name");
                String lastName = oauth2User.getAttribute("last_name");
                if (firstName != null) {
                    if (lastName != null) {
                        return firstName + " " + lastName;
                    }
                    return firstName;
                }
                return oauth2User.getAttribute("name");
        }
    }

    /**
     * Extract first name from OAuth2User attributes based on provider
     */
    private String extractFirstName(OAuth2User oauth2User, String provider) {
        switch (provider) {
            case "google":
                return oauth2User.getAttribute("given_name");
            case "facebook":
                return oauth2User.getAttribute("first_name");
            default:
                String name = oauth2User.getAttribute("name");
                return name != null ? name.split(" ")[0] : null;
        }
    }

    /**
     * Extract last name from OAuth2User attributes based on provider
     */
    private String extractLastName(OAuth2User oauth2User, String provider) {
        switch (provider) {
            case "google":
                return oauth2User.getAttribute("family_name");
            case "facebook":
                return oauth2User.getAttribute("last_name");
            default:
                String name = oauth2User.getAttribute("name");
                if (name != null && name.contains(" ")) {
                    String[] parts = name.split(" ");
                    return parts[parts.length - 1];
                }
                return null;
        }
    }

    /**
     * Extract profile picture URL from OAuth2User attributes based on provider
     */
    private String extractPictureUrl(OAuth2User oauth2User, String provider) {
        switch (provider) {
            case "google":
                return oauth2User.getAttribute("picture");
            case "facebook":
                Map<String, Object> picture = oauth2User.getAttribute("picture");
                if (picture != null && picture instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) picture.get("data");
                    if (data != null) {
                        return (String) data.get("url");
                    }
                }
                return null;
            default:
                return null;
        }
    }

    /**
     * Extract provider-specific user ID from OAuth2User attributes
     */
    private String extractProviderId(OAuth2User oauth2User, String provider) {
        switch (provider) {
            case "google":
                return oauth2User.getAttribute("sub");
            case "facebook":
                return oauth2User.getAttribute("id");
            default:
                return oauth2User.getAttribute("id");
        }
    }

    /**
     * Generate a placeholder email when provider doesn't supply one
     */
    private String generatePlaceholderEmail(String provider, String providerId) {
        if (providerId == null) {
            providerId = "unknown_" + System.currentTimeMillis();
        }
        return provider + "_" + providerId + "@placeholder.com";
    }

    /**
     * Generate a name from first/last name components or use providerId
     */
    private String generateName(String firstName, String lastName, String providerId) {
        if (firstName != null) {
            if (lastName != null) {
                return firstName + " " + lastName;
            }
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else if (providerId != null) {
            return "User_" + providerId;
        } else {
            return "Unknown User";
        }
    }

    /**
     * Get the appropriate attribute key for extracting the user's name based on provider
     */
    private String getNameAttributeKeyForProvider(String provider) {
        switch (provider) {
            case "facebook":
                return "id"; // Facebook always has an ID
            case "google":
                return "sub"; // Google uses 'sub' as unique identifier
            default:
                return "id"; // Default fallback
        }
    }

    private void checkAccountStatus(User user) {
        if (user.getAccountStatus() == AccountStatusEnum.Suspended) {
            LocalDateTime now = LocalDateTime.now();
            if (user.getSuspensionEndTime() != null) {
                if (now.isBefore(user.getSuspensionEndTime())) {
                    Duration duration = Duration.between(now, user.getSuspensionEndTime());
                    long hours = duration.toHours();
                    long minutes = duration.toMinutesPart();

                    String timeRemaining = hours + " giờ " + minutes + " phút";
                    String reason = user.getSuspensionReason() != null ?
                        user.getSuspensionReason() : "Vi phạm quy định của nền tảng";

                    throw new AccountSuspendedException(
                            "Tài khoản của bạn đã bị tạm khóa. Thời gian còn lại: " +
                                    timeRemaining + ". Lý do: " + reason);
                } else {
                    // Suspension period is over, reactivate the account
                    user.setAccountStatus(AccountStatusEnum.Active);
                    user.setSuspensionEndTime(null);
                    user.setSuspensionReason(null);
                }
            } else {
                // Permanent suspension
                String reason = user.getSuspensionReason() != null ?
                    user.getSuspensionReason() : "Vi phạm quy định của nền tảng";
                throw new AccountSuspendedException(
                        "Tài khoản của bạn đã bị tạm khóa vĩnh viễn. Lý do: " + reason);
            }
        }
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
        additionalInfo.put("authProvider", updatedUser.getAuthProvider().name().toLowerCase()); // Ensure lowercase

        String token = jwtService.generateToken(additionalInfo, updatedUser);
        
        // Use builder or provide all constructor arguments
        return TokenResponse.builder()
                .token(token)
                .user(updatedUser)
                .expiresIn(3600) // 1 hour in seconds
                .tokenType("Bearer")
                .build();
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

    /**
     * Get registration status information for a user
     * @param userId User ID to check
     * @return Map with registration status information
     */
    public Map<String, Object> getRegistrationStatus(Integer userId) {
        User user = userService.getUserById(userId);

        // Validate that this is an OAuth2 user
        if (user.getAuthProvider() == null || user.getAuthProvider() == AuthProvider.local) {
            throw new IllegalArgumentException("This user did not register via OAuth2.");
        }

        boolean isProfileIncomplete = isProfileIncomplete(user);
        boolean isPhonePlaceholder = user.getPhoneNumber() != null && user.getPhoneNumber().startsWith("placeholder_");

        Map<String, Object> status = new HashMap<>();
        status.put("userId", user.getUserId());
        status.put("email", user.getEmail());
        status.put("authProvider", user.getAuthProvider().toString().toLowerCase());
        status.put("isProfileComplete", !isProfileIncomplete && !isPhonePlaceholder);

        // Identify which fields are missing or need updating
        Map<String, Boolean> missingFields = new HashMap<>();
        missingFields.put("phoneNumber", user.getPhoneNumber() == null || isPhonePlaceholder);
        missingFields.put("dateOfBirth", user.getDob() == null);
        missingFields.put("verification", user.getRole() != RoleEnum.Admin && !user.getIsVerified());

        status.put("missingFields", missingFields);
        status.put("registrationSteps", getRegistrationSteps(user));

        return status;
    }

    /**
     * Generate registration steps based on user status
     * @param user User to generate steps for
     * @return List of registration steps with completion status
     */
    private List<Map<String, Object>> getRegistrationSteps(User user) {
        List<Map<String, Object>> steps = new ArrayList<>();
        boolean isPhonePlaceholder = user.getPhoneNumber() != null && user.getPhoneNumber().startsWith("placeholder_");

        // Step 1: OAuth2 Authentication (always completed if we're here)
        Map<String, Object> step1 = new HashMap<>();
        step1.put("step", 1);
        step1.put("title", "OAuth2 Authentication");
        step1.put("description", "Authenticate with OAuth2 provider");
        step1.put("completed", true);
        steps.add(step1);

        // Step 2: Complete profile
        Map<String, Object> step2 = new HashMap<>();
        step2.put("step", 2);
        step2.put("title", "Complete Profile");
        step2.put("description", "Provide required personal information");
        step2.put("completed", user.getDob() != null && user.getPhoneNumber() != null && !isPhonePlaceholder);
        steps.add(step2);

        // Step 3: Verification (only for customers and sellers)
        if (user.getRole() != RoleEnum.Admin) {
            Map<String, Object> step3 = new HashMap<>();
            step3.put("step", 3);
            step3.put("title", "Account Verification");
            step3.put("description", "Submit required verification information");
            step3.put("completed", user.getIsVerified());
            steps.add(step3);
        }

        return steps;
    }
}
