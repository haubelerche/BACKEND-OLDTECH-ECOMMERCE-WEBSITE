package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.User.SuspendUserRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.User.UpdateUserProfileRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.User.ChangePasswordRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AuthProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatus;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Exception.AccountSuspendedException;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.*;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.*;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification.VerificationRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Random;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ComplaintRepository complaintRepository;
    private final NotificationRepository notificationRepository;
    private final VerificationDetailRepository verificationDetailRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ComplaintRepository complaintRepository, NotificationRepository notificationRepository, VerificationDetailRepository verificationDetailRepository, AddressRepository addressRepository, OrderRepository orderRepository, RefundRepository refundRepository, SellerRepository sellerRepository, ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.complaintRepository = complaintRepository;
        this.notificationRepository = notificationRepository;
        this.verificationDetailRepository = verificationDetailRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.refundRepository = refundRepository;
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

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
        user.setAccountStatus(AccountStatusEnum.Active);
        user.setAuthProvider(AuthProvider.valueOf("local"));

        if (registerRequest.getDob() != null) {
            user.setDob(new java.sql.Date(registerRequest.getDob().getTime()));
        }


        user.setAvatarUrl(registerRequest.getAvatarUrl());

      
        // Set role to Customer by default
        user.setRole(RoleEnum.Customer);

        user.setIsVerified(false);

        User savedUser = userRepository.save(user);
        
        // Send welcome notification
        sendWelcomeNotification(savedUser);
        
        // Send profile completion notification if profile is incomplete
        if (isProfileIncomplete(savedUser)) {
            sendProfileCompletionNotification(savedUser);
        }
        
        // Send verification notification
        sendVerificationRequiredNotification(savedUser);
        
        return savedUser;
    }
    
    // Changed from private to public to allow access from controllers
    public boolean isProfileIncomplete(User user) {
        // Check required fields for a complete profile
        return user.getPhoneNumber() == null || 
               user.getDob() == null ||
               (!user.getIsVerified() && 
                (user.getRole() == RoleEnum.Seller || user.getRole() == RoleEnum.Customer));
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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return (UserDetails) userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    @Transactional
    public User processOAuth2User(OAuth2User oauth2User, String provider) {
        // Normalize provider name correctly to match enum values exactly
        AuthProvider normalizedProvider;
        if (provider.equalsIgnoreCase("google")) {
            normalizedProvider = AuthProvider.google;
        } else if (provider.equalsIgnoreCase("facebook")) {
            normalizedProvider = AuthProvider.facebook;
        } else {
            normalizedProvider = AuthProvider.local; // Default fallback
            log.warn("Unknown OAuth2 provider: {}. Defaulting to 'local'", provider);
        }

        // Facebook sometimes doesn't provide email if permissions aren't granted
        // or if the user hasn't verified their email on Facebook
        String email = oauth2User.getAttribute("email");
        final String providerId = extractProviderId(oauth2User, normalizedProvider.name());

        // Handle case where Facebook doesn't return email attribute
        if (email == null && normalizedProvider == AuthProvider.facebook) {
            log.warn("Facebook OAuth2 login - email attribute is null");
            // Generate a placeholder email using the Facebook ID
            if (providerId != null) {
                email = "facebook_" + providerId + "@placeholder.com";
                log.info("Generated placeholder email for Facebook user: {}", email);
            } else {
                email = "facebook_" + UUID.randomUUID().toString() + "@placeholder.com";
                log.info("Generated fallback placeholder email for Facebook user: {}", email);
            }
        }

        final String accessToken = oauth2User.getAttribute("access_token");
        final String refreshToken = oauth2User.getAttribute("refresh_token");
        final Timestamp tokenExpiryTimestamp = extractTokenExpiry(oauth2User);
        
        final String pictureUrl = extractProfilePicture(oauth2User, normalizedProvider.name());

        log.info("Processing OAuth2 user with email: {}, provider: {}", email, normalizedProvider);

        // Check if the user exists with the email regardless of auth provider
        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            log.info("Found existing user with email {}, auth provider: {}", email, existingUser.getAuthProvider());

            // Check if this is a locally registered user trying to use OAuth2
            if (existingUser.getAuthProvider() != null &&
                existingUser.getAuthProvider() == AuthProvider.local) {
                // Link the OAuth2 provider to this local account
                log.info("Linking OAuth2 {} provider to existing local account: {}", normalizedProvider, email);
                existingUser.setAuthProvider(normalizedProvider);
                existingUser.setAuthProviderId(providerId);
                existingUser.setAuthProviderToken(accessToken);
                existingUser.setAuthProviderRefreshToken(refreshToken);
                existingUser.setAuthProviderTokenExpires(tokenExpiryTimestamp);

                // Update avatar if user doesn't have one
                if (existingUser.getAvatarUrl() == null && pictureUrl != null) {
                    existingUser.setAvatarUrl(pictureUrl);
                }

                // We need to preserve the existing password for hybrid login capability
                return userRepository.save(existingUser);
            }

            // Update OAuth2 info for existing OAuth2 user
            updateOAuth2UserInfo(existingUser, normalizedProvider, providerId, accessToken, refreshToken,
                               tokenExpiryTimestamp, pictureUrl);

            return userRepository.save(existingUser);
        }

        // Create new user if not found
        User newUser = createNewOAuth2User(oauth2User, email, normalizedProvider, providerId, accessToken,
                                         refreshToken, tokenExpiryTimestamp, pictureUrl);

        log.info("Created new OAuth2 user: {}", email);
        return userRepository.save(newUser);
    }

    private String extractProviderId(OAuth2User oauth2User, String provider) {
        // First try standard "sub" claim
        String providerId = oauth2User.getAttribute("sub");

        // For Facebook the ID is usually just "id"
        if (providerId == null) {
            providerId = oauth2User.getAttribute("id");
        }

        // If still null, try looking in other common locations
        if (providerId == null && "facebook".equalsIgnoreCase(provider)) {
            log.debug("Attempting to find Facebook provider ID in alternate locations");
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> profileMap = oauth2User.getAttribute("profile");
                if (profileMap != null && profileMap.containsKey("id")) {
                    providerId = (String) profileMap.get("id");
                }
            } catch (Exception e) {
                log.warn("Error extracting Facebook provider ID: {}", e.getMessage());
            }
        }

        if (providerId == null) {
            // Generate a fallback ID if everything else fails
            providerId = "oauth2_" + System.currentTimeMillis();
            log.warn("Could not extract provider ID for {}. Using generated ID: {}", provider, providerId);
        }

        return providerId;
    }

    private Timestamp extractTokenExpiry(OAuth2User oauth2User) {
        Long tokenExpires = oauth2User.getAttribute("token_expires");
        if (tokenExpires != null) {
            return new Timestamp(tokenExpires);
        } else {
            // Default 1 hour if not specified
            return Timestamp.from(Instant.now().plusSeconds(3600));
        }
    }

    private String extractProfilePicture(OAuth2User oauth2User, String provider) {
        if ("google".equals(provider)) {
            return oauth2User.getAttribute("picture");
        } else if ("facebook".equals(provider)) {
            log.debug("Extracting Facebook profile picture. Available attributes: {}", oauth2User.getAttributes().keySet());
            try {
                // First try to get picture as direct attribute which might be a URL string
                Object pictureAttr = oauth2User.getAttribute("picture");

                if (pictureAttr != null) {
                    // If it's a simple string, use it directly
                    if (pictureAttr instanceof String) {
                        return (String) pictureAttr;
                    }
                    // If it's a Map (Facebook's nested structure), extract the URL from it
                    else if (pictureAttr instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> pictureMap = (Map<String, Object>) pictureAttr;
                        log.debug("Facebook picture object found: {}", pictureMap);

                        // Navigate through the nested structure
                        if (pictureMap.containsKey("data")) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> data = (Map<String, Object>) pictureMap.get("data");
                            if (data != null && data.containsKey("url")) {
                                return (String) data.get("url");
                            }
                        }
                    }
                }

                // Try looking for profile image in other formats or locations
                @SuppressWarnings("unchecked")
                Map<String, Object> profileMap = oauth2User.getAttribute("profile");
                if (profileMap != null && profileMap.containsKey("picture")) {
                    Object profilePicture = profileMap.get("picture");
                    if (profilePicture instanceof String) {
                        return (String) profilePicture;
                    }
                }
            } catch (Exception e) {
                log.warn("Error extracting Facebook profile picture: {}", e.getMessage());
                log.debug("Facebook attributes dump for debugging: {}", oauth2User.getAttributes());
            }
        }

        // Default fallback - no picture found
        log.info("No profile picture found for {} provider", provider);
        return null;
    }

    private User createNewOAuth2User(OAuth2User oauth2User, String email, AuthProvider provider,
                                   String providerId, String accessToken, String refreshToken,
                                   Timestamp tokenExpiryTimestamp, String pictureUrl) {
        User newUser = new User();
        newUser.setEmail(email != null ? email : "oauth2_" + UUID.randomUUID().toString() + "@placeholder.com");

        // Extract name from OAuth2 provider or create placeholder values
        String fullName = oauth2User.getAttribute("name");
        if (fullName == null || fullName.trim().isEmpty()) {
            // If no name is provided, use email prefix or generate placeholder
            fullName = (email != null) ? email.split("@")[0] : "User_" + UUID.randomUUID().toString().substring(0, 8);
        }

        // Always ensure we have first and last name values to satisfy @NotBlank constraints
        String[] nameParts = fullName.split(" ", 2);
        newUser.setFirstName(nameParts[0]);
        newUser.setLastName(nameParts.length > 1 ? nameParts[1] : "User");

        // Generate a valid phone number for OAuth2 users that matches the pattern ^\\+?[0-9]{10,15}$
        // Just using digits to comply with the validation pattern
        String randomDigits = generateRandomDigits(12);  // Generate 12 random digits
        String oauthPhone = "+" + randomDigits;  // Add + prefix to make it look like an international number
        newUser.setPhoneNumber(oauthPhone);

        // Set auth provider information
        newUser.setAuthProvider(provider);
        newUser.setAuthProviderId(providerId);
        newUser.setAuthProviderToken(accessToken);
        newUser.setAuthProviderRefreshToken(refreshToken);
        newUser.setAuthProviderTokenExpires(tokenExpiryTimestamp);

        // Set profile picture if available
        newUser.setAvatarUrl(pictureUrl);

        // Set account status and role
        newUser.setAccountStatus(AccountStatusEnum.Active);
        newUser.setRole(RoleEnum.Customer);
        newUser.setIsVerified(false);

        // Set timestamps
        Timestamp now = Timestamp.from(Instant.now());
        newUser.setCreatedAt(now);
        newUser.setUpdatedAt(now);
        newUser.setLastLogin(now);

        log.info("Created new user via OAuth2 provider: {}, with placeholder phone: {}",
                provider.name(), oauthPhone);

        return newUser;
    }

    // Helper method to generate random digits for phone number
    private String generateRandomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        // First digit shouldn't be 0
        sb.append(1 + random.nextInt(9));

        // Rest of the digits
        for (int i = 1; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void updateOAuth2UserInfo(User user, AuthProvider provider, String providerId,
                                    String accessToken, String refreshToken,
                                    Timestamp tokenExpiryTimestamp, String pictureUrl) {
        // Use the provider enum directly
        user.setAuthProvider(provider);
        user.setAuthProviderId(providerId);
        user.setAuthProviderToken(accessToken);
        user.setAuthProviderRefreshToken(refreshToken);
        user.setAuthProviderTokenExpires(tokenExpiryTimestamp);

        // Update avatar URL if not set or if from same provider
        if (user.getAvatarUrl() == null ||
            (pictureUrl != null && user.getAuthProvider() == provider)) {
            user.setAvatarUrl(pictureUrl);
        }

        user.setLastLogin(Timestamp.from(Instant.now()));
    }

    // Get all users method
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Search methods
    public List<User> searchUsersByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }

    public List<User> searchUsersByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return userRepository.findByEmailContainingIgnoreCase(email);
    }

    public List<User> searchUsersByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return userRepository.findByPhoneNumberContaining(phoneNumber);
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác.");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
    }

    @Transactional
    public void deactivateAccount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        user.setAccountStatus(AccountStatusEnum.Inactive);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
    }

    @Transactional
    public VerificationDetail submitVerificationDocuments(Integer userId, VerificationRequest verificationRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (user.getIsVerified()) {
            throw new IllegalStateException("Người dùng đã được xác thực trước đó.");
        }

        VerificationDetail verificationDetail = verificationDetailRepository.findByUser(user)
                .orElseGet(() -> VerificationDetail.builder()
                        .user(user)
                        .isApproved(false)
                        .build());

        // Update verification details with the documents provided
        verificationDetail.setSelfiePicUrl(verificationRequest.getSelfiePicUrl());
        verificationDetail.setFrontImageUrl(verificationRequest.getFrontImageUrl());
        verificationDetail.setBackImageUrl(verificationRequest.getBackImageUrl());
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // Save the verification details
        VerificationDetail savedDetail = verificationDetailRepository.save(verificationDetail);

        // Send notification to admin about new verification request
        sendVerificationRequestSubmittedNotification(user);

        log.info("Đã gửi tài liệu xác thực cho người dùng ID: {}", userId);
        return savedDetail;
    }

    @Transactional
    public void fileComplaint(Integer userId, String complaintText) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId + ", cannot file complaint."));

        Complaint newComplaint = new Complaint();
        newComplaint.setComplainantId(userId);
        newComplaint.setReason(complaintText);
        newComplaint.setStatus(ComplaintStatus.Pending);
        newComplaint.setCreatedAt(Timestamp.from(Instant.now()));
        newComplaint.setUpdatedAt(Timestamp.from(Instant.now()));
        complaintRepository.save(newComplaint);

        log.info("Đơn khiếu nại đã được gửi bởi người dùng ID: {}", userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại với ID: " + userId + ", không thể lấy thông báo."));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void markNotificationRead(Integer userId, Long notificationId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId + ", không thể đánh dấu thông báo."));

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông báo với ID: " + notificationId));

        // Verify the notification belongs to the user
        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("Người dùng " + userId + " không được phép đánh dấu thông báo " + notificationId + " là đã đọc.");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("Thông báo {} đã được đánh dấu là đã đọc cho người dùng {}", notificationId, userId);
        } else {
            log.info("Thông báo {} đã được đánh dấu là đã đọc trước đó cho người dùng {}", notificationId, userId);
        }
    }

    @Transactional
    public void deleteAccount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        log.info("Starting deletion process for user ID: {}", userId);

        // --- Delete dependent entities FIRST ---
        try {
            // Notifications
            List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
            if (!notifications.isEmpty()) {
                notificationRepository.deleteAll(notifications);
                log.info("Deleted {} notifications for user {}", notifications.size(), userId);
            }

            // Complaints (as complainant)
            List<Complaint> complaintsAsComplainant = complaintRepository.findByComplainantId(userId);
            if (!complaintsAsComplainant.isEmpty()) {
                complaintRepository.deleteAll(complaintsAsComplainant);
                log.info("Deleted {} complaints (as complainant) for user {}", complaintsAsComplainant.size(), userId);
            }

            // Verification Details
            verificationDetailRepository.findByUser(user).ifPresent(detail -> {
                verificationDetailRepository.delete(detail);
                log.info("Deleted verification details for user {}", userId);
            });

            // Addresses
            List<Address> addresses = addressRepository.findByUserId(userId);
            if (!addresses.isEmpty()) {
                addressRepository.deleteAll(addresses);
                log.info("Deleted {} addresses for user {}", addresses.size(), userId);
            }

            // Orders
            List<Orders> orders = orderRepository.findByUserId(userId);
            if (!orders.isEmpty()) {
                 // Assuming OrderDetail is handled by cascade or is deleted here if needed
                 orderRepository.deleteAll(orders);
                 log.info("Deleted {} orders for user {}", orders.size(), userId);
            }

            // Refunds
            List<Refund> refunds = refundRepository.findByUserId(userId);
            if (!refunds.isEmpty()) {
                refundRepository.deleteAll(refunds);
                log.info("Deleted {} refunds for user {}", refunds.size(), userId);
            }

            // --- Handle Seller related data ---
            if (user.getRole() == RoleEnum.Seller) {
                 log.info("User {} is a seller. Proceeding with seller data cleanup.", userId);

                 // Find seller record (assuming sellerId = userId)
                 Optional<Seller> sellerOpt = sellerRepository.findById(userId);
                 if (sellerOpt.isPresent()) {
                     Seller seller = sellerOpt.get();
                     Integer sellerId = seller.getSellerId();

                     // Find and delete Products associated with this seller
                     List<Product> products = productRepository.findBySellerId(sellerId);
                     if (!products.isEmpty()) {
                         log.info("Found {} products for seller ID {}. Deleting products and their images.", products.size(), sellerId);
                         for (Product product : products) {
                             // Delete Product Images first
                             List<ProductImage> productImages = productImageRepository.findByProductOrderByDisplayOrderAsc(product);
                             if (!productImages.isEmpty()) {
                                 // TODO: Add logic here to delete images from storage (S3, local, etc.)
                                 productImageRepository.deleteAll(productImages);
                                 log.info("Deleted {} images for product ID {}", productImages.size(), product.getProductId());
                             }
                             // Delete Product
                             productRepository.delete(product);
                         }
                         log.info("Finished deleting products for seller ID {}", sellerId);
                     }

                     // Delete the Seller record itself
                     sellerRepository.delete(seller);
                     log.info("Deleted seller record for seller ID {}", sellerId);
                 } else {
                     log.warn("User {} has Seller role but no corresponding Seller record found with ID {}. Skipping seller cleanup.", userId, userId);
                 }
            }

            // --- Anonymize the User entity LAST ---
            user.setAccountStatus(AccountStatusEnum.Deleted);
            user.setEmail("deleted_" + userId + "@anonymized.com");
            user.setPhoneNumber(null);
            user.setFirstName("Deleted");
            user.setLastName("User");
            user.setPassword(null); // Consider security implications
            user.setAvatarUrl(null);
            user.setRefundMomoAccount(null);
            user.setAuthProvider(null);
            user.setAuthProviderId(null);
            user.setAuthProviderToken(null);
            user.setAuthProviderRefreshToken(null);
            user.setAuthProviderTokenExpires(null);
            user.setRole(RoleEnum.Customer); // Reset role
            user.setIsVerified(false);
            user.setUpdatedAt(Timestamp.from(Instant.now()));

            userRepository.save(user);
            log.info("User account {} has been successfully anonymized and marked as Deleted.", userId);

        } catch (Exception e) {
            log.error("Error during deletion process for user account {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xóa tài khoản: " + e.getMessage(), e);
        }
    }

    private void sendVerificationRequestSubmittedNotification(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Yêu cầu xác thực tài khoản của bạn đã được gửi. Vui lòng chờ quản trị viên phê duyệt, chúng tôi sẽ thông báo cho bạn sớm.");
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notification.setRead(false);
        notification.setNotificationType(NotificationTypeEnum.AdminMessage);
        notificationRepository.save(notification);

        log.info("Sent verification request submitted notification to user ID: {}", user.getUserId());
    }

    @Transactional
    public User updateUserForOAuth2(User user) {
        log.info("Updating user for OAuth2: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getVerificationStatus(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("isVerified", user.getIsVerified());

        // Get verification detail if it exists
        verificationDetailRepository.findByUser(user).ifPresent(detail -> {
            statusInfo.put("verificationDetailId", detail.getVerifyId());
            statusInfo.put("documentSubmitted", detail.getSelfiePicUrl() != null &&
                                              detail.getFrontImageUrl() != null &&
                                              detail.getBackImageUrl() != null);
            statusInfo.put("updatedAt", detail.getUpdatedAt());
            statusInfo.put("createdAt", detail.getCreatedAt());
        });

        return statusInfo;
    }

    @Transactional
    public void updateProfilePicture(Integer userId, String imageUrl) {
        log.info("Updating profile picture for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        user.setAvatarUrl(imageUrl);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);

        log.info("Profile picture updated successfully for user ID: {}", userId);
    }

    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        log.info("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }



    @Transactional
    public void changeUserPassword(User user, String newPassword) {
        log.info("Changing password for user: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", user.getUserId());
    }

    /**
     * Update user profile with provided data
     * Automatically checks if phone/DOB are valid and complete
     * Verified users cannot change their name, DOB, or verification images
     */
    @Transactional
    public User updateUserProfile(Integer userId, Map<String, Object> profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        boolean isVerified = user.getIsVerified() != null && user.getIsVerified();

        // Update basic user information if provided and user is not verified
        if (!isVerified) {
            // Only allow name changes if the user is not verified
            if (profileData.containsKey("firstName")) {
                user.setFirstName((String) profileData.get("firstName"));
            }

            if (profileData.containsKey("lastName")) {
                user.setLastName((String) profileData.get("lastName"));
            }

            // Only allow DOB changes if the user is not verified
            if (profileData.containsKey("dob")) {
                Object dobValue = profileData.get("dob");
                java.sql.Date dobDate = null;

                if (dobValue instanceof Long) {
                    dobDate = new java.sql.Date((Long) dobValue);
                } else if (dobValue instanceof String) {
                    try {
                        // Try parsing as ISO date string
                        dobDate = java.sql.Date.valueOf((String) dobValue);
                    } catch (Exception e) {
                        log.error("Failed to parse DOB string: {}", dobValue);
                        throw new IllegalArgumentException("Định dạng ngày sinh không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD.");
                    }
                }

                if (dobDate != null) {
                    user.setDob(dobDate);
                }
            }
        } else if (profileData.containsKey("firstName") || profileData.containsKey("lastName") || profileData.containsKey("dob")) {
            // If the user is verified and attempts to change these restricted fields, throw an exception
            log.warn("User ID {} is verified and attempting to change restricted profile fields", userId);
            throw new IllegalStateException("Bạn không thể thay đổi họ tên hoặc ngày sinh sau khi tài khoản đã được xác thực.");
        }

        // Handle phone number updates - always allow updates but validate uniqueness
        if (profileData.containsKey("phoneNumber")) {
            String newPhone = (String) profileData.get("phoneNumber");
            // Check if new phone exists for another user
            if (!newPhone.equals(user.getPhoneNumber()) && existsByPhoneNumber(newPhone)) {
                throw new IllegalArgumentException("Số điện thoại này đã được sử dụng bởi tài khoản khác.");
            }
            user.setPhoneNumber(newPhone);
        }

        // Handle email updates - always allow updates but validate uniqueness
        if (profileData.containsKey("email")) {
            String newEmail = (String) profileData.get("email");
            // Check if new email exists for another user
            if (!newEmail.equals(user.getEmail()) && existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email này đã được sử dụng bởi tài khoản khác.");
            }
            user.setEmail(newEmail);
            log.info("Updated email for user ID: {} to: {}", userId, newEmail);
        }

        // Handle avatar URL updates - always allow
        if (profileData.containsKey("avatarUrl")) {
            user.setAvatarUrl((String) profileData.get("avatarUrl"));
        }

        // Handle refund MoMo account updates - always allow
        if (profileData.containsKey("refundMomoAccount")) {
            user.setRefundMomoAccount((String) profileData.get("refundMomoAccount"));
        }

        // Update timestamp
        user.setUpdatedAt(Timestamp.from(Instant.now()));

        // Save and return the updated user
        return userRepository.save(user);
    }

    /**
     * Send notification to user when their profile is submitted for verification
     */
    @Transactional
    public void sendProfileSubmittedForVerificationNotification(User user) {
        // Create notification for user
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Hồ sơ của bạn đã được gửi tới admin để xác thực. Bạn sẽ nhận được thông báo khi quá trình xác thực hoàn tất.");
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notification.setRead(false);
        notification.setNotificationType(NotificationTypeEnum.AdminMessage);
        notificationRepository.save(notification);

        // Create verification details record in the database
        VerificationDetail verificationDetail = new VerificationDetail();
        verificationDetail.setUser(user);
        verificationDetail.setIsApproved(null); // Pending approval
        verificationDetail.setSelfiePicUrl(user.getSelfiePicUrl());
        verificationDetail.setFrontImageUrl(user.getFrontImageUrl());
        verificationDetail.setBackImageUrl(user.getBackImageUrl());
        verificationDetail.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // Save the verification detail to database
        verificationDetailRepository.save(verificationDetail);

        log.info("Sent profile submitted for verification notification to user ID: {}", user.getUserId());
        log.info("Created verification details record for user ID: {}", user.getUserId());

        // Also send notification to admin about the new profile verification request
        sendAdminNotificationForVerificationRequest(user);
    }

    /**
     * Send notification to admin for new verification requests
     */
    private void sendAdminNotificationForVerificationRequest(User requestingUser) {
        // Get all admin users
        List<User> adminUsers = userRepository.findByRole(RoleEnum.Admin);

        for (User admin : adminUsers) {
            Notification notification = new Notification();
            notification.setUser(admin);
            notification.setMessage("Yêu cầu xác thực mới từ người dùng: " + requestingUser.getFirstName() + " " +
                                   requestingUser.getLastName() + " (ID: " + requestingUser.getUserId() + ")");
            notification.setCreatedAt(Timestamp.from(Instant.now()));
            notification.setRead(false);
            notification.setNotificationType(NotificationTypeEnum.System);
            notificationRepository.save(notification);

            log.info("Sent verification request notification to admin ID: {}", admin.getUserId());
        }
    }
}
