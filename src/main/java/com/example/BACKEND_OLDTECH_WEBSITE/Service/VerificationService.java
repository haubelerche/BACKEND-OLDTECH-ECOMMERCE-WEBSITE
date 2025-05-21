package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification.VerificationRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification.VerificationResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Notification;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.VerificationDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.VerificationDetailRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class VerificationService {

    private final VerificationDetailRepository verificationDetailRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public VerificationService(VerificationDetailRepository verificationDetailRepository,
                               UserRepository userRepository,
                               NotificationRepository notificationRepository) {
        this.verificationDetailRepository = verificationDetailRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public VerificationDetail initiateVerificationRequest(User user) {
        if (user.getIsVerified()) {
            sendNotification(user, "Your account is already verified.");
            // Optionally return existing detail if needed for other purposes or throw exception
            return verificationDetailRepository.findByUser(user).orElse(null); 
        }

        Optional<VerificationDetail> existingDetailOpt = verificationDetailRepository.findByUser(user);
        VerificationDetail verificationDetail;
        if (existingDetailOpt.isPresent()) {
            verificationDetail = existingDetailOpt.get();
            if (verificationDetail.getIsApproved() != null && !verificationDetail.getIsApproved() && !user.getIsVerified()) {
                clearVerificationData(verificationDetail); // Removed note as it's not stored
            }
        } else {
            verificationDetail = VerificationDetail.builder()
                    .user(user)
                    .isApproved(false)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
        }
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        VerificationDetail savedDetail = verificationDetailRepository.save(verificationDetail);
        sendNotification(user, "Bạn hãy hoàn tất việc xác thực danh tính bằng cách chụp ảnh mặt và ảnh hai mặt CCCD của mình.");
        return savedDetail;
    }

    private void clearVerificationData(VerificationDetail vd) {
        vd.setSelfiePicUrl(null);
        vd.setFrontImageUrl(null);
        vd.setBackImageUrl(null);
        vd.setIsApproved(false);
    }

    @Transactional
    public VerificationDetail submitVerificationDocuments(Integer userIdRequesting, VerificationRequest docsDto) {
        User user = userRepository.findById(userIdRequesting)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userIdRequesting));     

        if (user.getIsVerified()) {
            throw new IllegalStateException("User ID: " + userIdRequesting + " đã được xác thực.");
        }

        VerificationDetail verificationDetail = verificationDetailRepository.findByUser(user)
                .orElseGet(() -> VerificationDetail.builder()
                                        .user(user)
                                        .isApproved(false)
                                        .createdAt(new Timestamp(System.currentTimeMillis()))
                                        .build());

        if (verificationDetail.getIsApproved() != null && verificationDetail.getIsApproved()) {
             throw new IllegalStateException("Thông tin đã được xác thực và được chấp nhận.");
        }
        

        verificationDetail.setSelfiePicUrl(docsDto.getSelfiePicUrl());
        verificationDetail.setFrontImageUrl(docsDto.getFrontImageUrl());
        verificationDetail.setBackImageUrl(docsDto.getBackImageUrl());
        verificationDetail.setIsApproved(false);
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        System.out.println("Thông tin đã được gửi để xác thực danh tính và sẽ được duyệt sớm nhất có thể");
        return verificationDetailRepository.save(verificationDetail);
    }

    @Transactional
    public VerificationDetail approveVerification(Integer verificationDetailId, Integer adminUserId) {
        VerificationDetail verificationDetail = verificationDetailRepository.findById(verificationDetailId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin xác thực mang ID này: " + verificationDetailId));

        User userToVerify = verificationDetail.getUser();
        if (userToVerify.getIsVerified() && verificationDetail.getIsApproved() != null && verificationDetail.getIsApproved()) {
            throw new IllegalStateException("User ID: " + userToVerify.getUserId() + " đã được xác thực và thông tin này đã được chấp nhận.");
        }
        if (verificationDetail.getSelfiePicUrl() == null || verificationDetail.getFrontImageUrl() == null || verificationDetail.getBackImageUrl() == null) {
             throw new IllegalStateException("Không thể chấp nhận xác thực danh tính cho User ID: " + userToVerify.getUserId() + " vì thông tin chưa được gửi đầy đủ trong thông tin này.");
        }

        // Update user verification status
        userToVerify.setIsVerified(true);
        userRepository.save(userToVerify);

        // Update verification detail
        verificationDetail.setIsApproved(true);
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        VerificationDetail savedDetail = verificationDetailRepository.save(verificationDetail);
        
        sendNotification(userToVerify, "Bravo! Yêu cầu xác thực danh tính của bạn đã được phê duyệt.");
        return savedDetail;
    }

    @Transactional
    public VerificationDetail rejectVerification(Integer verificationDetailId, Integer adminUserId, String adminResponse) {
        VerificationDetail verificationDetail = verificationDetailRepository.findById(verificationDetailId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin xác thực mang ID này: " + verificationDetailId));

        User userToUpdate = verificationDetail.getUser();
        if (verificationDetail.getSelfiePicUrl() == null || verificationDetail.getFrontImageUrl() == null || verificationDetail.getBackImageUrl() == null) {
             throw new IllegalStateException("Từ chối xác thực cho User ID: " + userToUpdate.getUserId() + " vì thông tin chưa được gửi đầy đủ.");
        }

        // Update user verification status
        userToUpdate.setIsVerified(false);
        userRepository.save(userToUpdate);

        // Update verification detail
        verificationDetail.setIsApproved(false);
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        VerificationDetail savedDetail = verificationDetailRepository.save(verificationDetail);
        
        sendNotification(userToUpdate, "Hồ sơ xác thực của bạn đã bị từ chối. Lý do: " + adminResponse + ". Vui lòng kiểm tra và gửi lại.");
        return savedDetail;
    }

    @Transactional(readOnly = true)
    public List<VerificationDetail> getPendingVerifications() {
        return verificationDetailRepository.findBySelfiePicUrlIsNotNullAndFrontImageUrlIsNotNullAndBackImageUrlIsNotNullAndIsApprovedFalseAndUser_IsVerifiedFalse(); // Updated method call
    }

    @Transactional(readOnly = true)
    public Optional<VerificationDetail> getVerificationDetailsByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng mang ID này: " + userId));
        return verificationDetailRepository.findByUser(user);
    }
    
    @Transactional(readOnly = true)
    public Optional<VerificationDetail> getVerificationDetailById(Integer verificationDetailId) {
        return verificationDetailRepository.findById(verificationDetailId);
    }

    @Transactional(readOnly = true)
    public VerificationResponse getVerificationResponseByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng mang ID này: " + userId));
        
        Optional<VerificationDetail> verificationDetailOpt = verificationDetailRepository.findByUser(user);
        if (verificationDetailOpt.isPresent()) {
            return mapToVerificationResponse(verificationDetailOpt.get());
        }
        
        return null;
    }
    
    public VerificationResponse mapToVerificationResponse(VerificationDetail detail) {
        VerificationResponse response = new VerificationResponse();
        response.setVerifyId(detail.getVerifyId());
        response.setUserId(detail.getUser().getUserId());
        response.setIsApproved(detail.getIsApproved());
        response.setSelfiePicUrl(detail.getSelfiePicUrl());
        response.setFrontImageUrl(detail.getFrontImageUrl());
        response.setBackImageUrl(detail.getBackImageUrl());
        response.setCreatedAt(detail.getCreatedAt());
        response.setUpdatedAt(detail.getUpdatedAt());
        return response;
    }

    private void sendNotification(User user, String message) {
        if (user != null && message != null && !message.isEmpty()) {
            Notification notification = new Notification(user, message);
            notificationRepository.save(notification);
            System.out.println("Thông báo tới người dùng " + user.getUserId() + ": " + message);
        } else {
            System.out.println("Không thể gửi thông báo: user hoặc thông báo rỗng.");
        }
    }

    @Transactional
    public VerificationDetail reviewVerification(Integer verificationDetailId, boolean isVerified, String adminResponse, Integer adminUserId) {
        VerificationDetail verificationDetail = verificationDetailRepository.findById(verificationDetailId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin xác thực mang ID này: " + verificationDetailId));
        User user = verificationDetail.getUser();

        // Save admin response to the entity
        verificationDetail.setAdminResponse(adminResponse);
        verificationDetail.setIsApproved(isVerified);
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        verificationDetailRepository.save(verificationDetail);
        user.setIsVerified(isVerified);
        userRepository.save(user);

        if (isVerified) {
            sendNotification(user, "Bravo! Yêu cầu xác thực danh tính của bạn đã được phê duyệt.");
        } else {
            sendNotification(user, "Hồ sơ xác thực của bạn đã bị từ chối. Lý do: " + (adminResponse != null ? adminResponse : "Không đáp ứng yêu cầu của hệ thống") + ". Vui lòng kiểm tra và gửi lại nếu cần thiết.");
        }
        return verificationDetail;
    }
}
