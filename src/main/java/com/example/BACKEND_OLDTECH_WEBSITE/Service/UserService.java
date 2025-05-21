package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.User.UpdateUserProfileRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.User.ChangePasswordRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AuthProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatus;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
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
import java.time.Instant;
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
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ComplaintRepository complaintRepository, NotificationRepository notificationRepository, VerificationDetailRepository verificationDetailRepository, AddressRepository addressRepository, OrderRepository orderRepository, RefundRepository refundRepository, SellerRepository sellerRepository, ProductRepository productRepository, ProductImageRepository productImageRepository, PasswordResetTokenRepository passwordResetTokenRepository) {
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
        this.passwordResetTokenRepository = passwordResetTokenRepository;
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
    
    private boolean isProfileIncomplete(User user) {
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
        final String email = oauth2User.getAttribute("email");
        final String providerId = extractProviderId(oauth2User, provider);
        
      
        final String accessToken = oauth2User.getAttribute("access_token");
        final String refreshToken = oauth2User.getAttribute("refresh_token");
        final Timestamp tokenExpiryTimestamp = extractTokenExpiry(oauth2User);
        
       
        final String pictureUrl = extractProfilePicture(oauth2User, provider);
        
        log.info("Processing OAuth2 user with email: {}, provider: {}", email, provider);
        
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> createNewOAuth2User(oauth2User, email, provider, providerId, accessToken, refreshToken, tokenExpiryTimestamp, pictureUrl));

      
        updateOAuth2UserInfo(user, provider, providerId, accessToken, refreshToken, tokenExpiryTimestamp, pictureUrl);
        
        log.info("Updated OAuth2 tokens for user: {}, provider: {}", email, provider);
        return userRepository.save(user);
    }
    
    private String extractProviderId(OAuth2User oauth2User, String provider) {
        String providerId = oauth2User.getAttribute("sub");
        if (providerId == null) {
       
            providerId = oauth2User.getAttribute("id");
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
            @SuppressWarnings("unchecked")
            Map<String, Object> picture = oauth2User.getAttribute("picture");
            if (picture != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) picture.get("data");
                if (data != null) {
                    return (String) data.get("url");
                }
            }
        }
        return null;
    }
    
    private User createNewOAuth2User(OAuth2User oauth2User, String email, String provider, 
                                   String providerId, String accessToken, String refreshToken, 
                                   Timestamp tokenExpiryTimestamp, String pictureUrl) {
        User newUser = new User();
        newUser.setEmail(email);
        
        // Split the name into first and last name ..
        String fullName = oauth2User.getAttribute("name");
        if (fullName == null) {
            fullName = email != null ? email.split("@")[0] : "User"; 
        }
        String[] nameParts = fullName.split(" ", 2);
        newUser.setFirstName(nameParts[0]);
        newUser.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        newUser.setAuthProvider(AuthProvider.valueOf(provider.toUpperCase()));
        newUser.setAuthProviderId(providerId);
        newUser.setAuthProviderToken(accessToken);
        newUser.setAuthProviderRefreshToken(refreshToken);
        newUser.setAuthProviderTokenExpires(tokenExpiryTimestamp);
        newUser.setAvatarUrl(pictureUrl);
        newUser.setAccountStatus(AccountStatusEnum.Active);
        newUser.setRole(RoleEnum.Customer);
        newUser.setIsVerified(false); // Require manual verification
        newUser.setCreatedAt(Timestamp.from(Instant.now()));
        newUser.setUpdatedAt(Timestamp.from(Instant.now()));
        newUser.setLastLogin(Timestamp.from(Instant.now()));
        
        log.info("Created new user via OAuth2 provider: {}", provider);
        return newUser;
    }
    
    private void updateOAuth2UserInfo(User user, String provider, String providerId, 
                                    String accessToken, String refreshToken, 
                                    Timestamp tokenExpiryTimestamp, String pictureUrl) {
        user.setAuthProvider(AuthProvider.valueOf(provider.toUpperCase()));
        user.setAuthProviderId(providerId);
        user.setAuthProviderToken(accessToken);
        user.setAuthProviderRefreshToken(refreshToken);
        user.setAuthProviderTokenExpires(tokenExpiryTimestamp);
        
        // Update avatar URL if not set or if from same provider
        if (user.getAvatarUrl() == null || 
            (pictureUrl != null && user.getAuthProvider().name().equalsIgnoreCase(provider))) {
            user.setAvatarUrl(pictureUrl);
        }
        
        user.setLastLogin(Timestamp.from(Instant.now()));
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }
    
    public User getUserByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với số điện thoại: " + phoneNumber);
        }
        return user;
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
    public void deleteAccount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

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
            // Consider handling complaints where user is respondent if necessary
            // List<Complaint> complaintsAsRespondent = complaintRepository.findByRespondentId(userId);
            // handle complaintsAsRespondent...

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
                     
                     // TODO: Delete Reviews where this seller is the sellerId if necessary
                     
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
            user.setRole(RoleEnum.Customer); // Reset role?
            user.setIsVerified(false);
            user.setUpdatedAt(Timestamp.from(Instant.now()));

            userRepository.save(user);
            log.info("User account {} has been successfully anonymized and marked as Deleted.", userId);

        } catch (Exception e) {
            log.error("Error during deletion process for user account {}: {}", userId, e.getMessage(), e);
            // Let Spring handle rollback due to @Transactional
            throw new RuntimeException("Lỗi khi xóa tài khoản: " + e.getMessage(), e);
        }
    }

    @Transactional
    public User updateUserProfile(Integer userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        // Handle Email Change with Uniqueness Check
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email " + request.getEmail() + " đã tồn tại.");
            }
            // Add regex pattern check if needed here, though @Pattern on User model handles it at persistence
            user.setEmail(request.getEmail());
        }

        // Handle Phone Number Change with Uniqueness Check
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("Số điện thoại " + request.getPhoneNumber() + " đã tồn tại.");
            } // Add regex pattern check if needed here, though @Pattern on User model handles it at persistence
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getDob() != null) {
            user.setDob(new java.sql.Date(request.getDob().getTime()));
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        // Handle RefundMomoAccount Change
        if (request.getRefundMomoAccount() != null) {
            user.setRefundMomoAccount(request.getRefundMomoAccount());
        }

     
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác."); // Or a custom exception
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
    }

    @Transactional
    public void requestVerification(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        if (user.getIsVerified()) {
            System.out.println("Người dùng " + userId + " đã được xác thực.");
          
            // throw new IllegalStateException("User is already verified.");
        } else {
            // Send a notification about the verification request
            sendVerificationRequestSubmittedNotification(user);
            
            System.out.println("Yêu cầu xác thực đã được gửi cho người dùng ID: " + userId + ". Chờ phê duyệt từ quản trị viên.");
            // user.setVerificationStatus(VerificationStatus.PENDING_APPROVAL); // Example if you have such a field
            // userRepository.save(user); // If any user state was changed
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

        System.out.println("Đơn khiếu nại đã được gửi bởi người dùng ID: " + userId + ". Khiếu nại: '" + complaintText + "'. Đây sẽ được lưu vào cơ sở dữ liệu.");
    }

    @Transactional(readOnly = true) 
    public List<Notification> getNotifications(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId + ", cannot retrieve notifications."));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void markNotificationRead(Integer userId, Long notificationId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId + ", cannot mark notification."));

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new EntityNotFoundException("Notification not found with ID: " + notificationId)); // Consider creating a specific NotificationNotFoundException

        // Verify the notification belongs to the user
        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("User " + userId + " is not authorized to mark notification " + notificationId + " as read."); // Or a more specific access denied exception
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            System.out.println("Notification " + notificationId + " marked as read for user " + userId);
        } else {
            System.out.println("Notification " + notificationId + " was already marked as read for user " + userId);
        }
    }

    @Transactional
    public User updateUserForOAuth2(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(Integer userId, RoleEnum newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        
        RoleEnum oldRole = user.getRole();
        user.setRole(newRole);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        
        User updatedUser = userRepository.save(user);
        
        // If user becomes a seller, send notification
        if (newRole == RoleEnum.Seller && oldRole != RoleEnum.Seller) {
            sendSellerRoleRequestNotification(updatedUser);
        }
        
        return updatedUser;
    }
    
    private void sendSellerRoleRequestNotification(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Yêu cầu xác thực tài khoản người bán của bạn đã được gửi. Vui lòng chờ quản trị viên phê duyệt, chúng tôi sẽ thông báo cho bạn sớm.");
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notification.setRead(false);
        notification.setNotificationType(NotificationTypeEnum.AdminMessage);
        notificationRepository.save(notification);
        
        log.info("Sent seller role request notification to user ID: {}", user.getUserId());
    }

    // Add this method to get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Search methods
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
    
    public List<User> searchUsersByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return userRepository.findAll();
        }
        
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
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

    @Transactional(readOnly = true)
    public Map<String, Object> getVerificationStatus(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("isVerified", user.getIsVerified());

        // Get verification detail if exists
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        
        user.setAvatarUrl(imageUrl);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
        
        log.info("Đã cập nhật ảnh đại diện cho người dùng ID: {}", userId);
    }

    @Transactional
    public String createPasswordResetTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user).orElse(null);
        if (existingToken != null) {
            existingToken.setToken(token);
            existingToken.setExpiryDate(existingToken.calculateExpiryDate(PasswordResetToken.EXPIRATION)); // Recalculate expiry
            passwordResetTokenRepository.save(existingToken);
        } else {
            PasswordResetToken myToken = new PasswordResetToken(token, user);
            passwordResetTokenRepository.save(myToken);
        }
        return token;
    }

    public PasswordResetToken getPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token).orElse(null);
    }

    public User getUserByPasswordResetToken(String token) {
        PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token).orElse(null);
        if (passToken == null) {
            return null;
        }
        return passToken.getUser();
    }

    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword)); // Make sure to hash the new password
        userRepository.save(user);
        // Invalidate the token after successful password change
        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);
    }

    // Add method to get user by email if it doesn't exist for AuthenticationService
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }


    @Transactional
    public void deactivateAccount(Integer userId, boolean isAdminAction) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (isAdminAction) {
            user.setAccountStatus(AccountStatusEnum.Suspended); // Admin ban/suspension
        } else {
            user.setAccountStatus(AccountStatusEnum.Inactive); // User self-deactivation
        }

        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
    }

    @Transactional
    public boolean canSelfReactivate(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        return user.getAccountStatus() != AccountStatusEnum.Suspended; // Can't reactivate if suspended by admin
    }

    @Transactional
    public void reactivateAccount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        user.setAccountStatus(AccountStatusEnum.Active);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
    }










}
