package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification.VerificationRequest;
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
            if (verificationDetail.getIsVerified() != null && !verificationDetail.getIsVerified() && !user.getIsVerified()) {
                clearVerificationData(verificationDetail); // Removed note as it's not stored
            }
        } else {
            verificationDetail = VerificationDetail.builder()
                    .user(user)
                    .isVerified(false) 
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
        }
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        VerificationDetail savedDetail = verificationDetailRepository.save(verificationDetail);
        sendNotification(user, "Bạn hãy hoàn tất việc xác thực danh tính bằng cách chụp ảnh mặt và ảnh hai mặt CCCD của mình.");
        return savedDetail;
    }

    private void clearVerificationData(VerificationDetail vd) {
        vd.setSelfiePicUrl(null); // Changed from setNationalId
        vd.setFrontImageUrl(null);
        vd.setBackImageUrl(null);
        vd.setIsVerified(false); 
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
                                        .isVerified(false) 
                                        .createdAt(new Timestamp(System.currentTimeMillis()))
                                        .build());

        if (verificationDetail.getIsVerified() != null && verificationDetail.getIsVerified()) {
             throw new IllegalStateException("Thông tin đã được xác thực và được chấp nhận.");
        }
        

        verificationDetail.setSelfiePicUrl(docsDto.getSelfiePicUrl());
        verificationDetail.setFrontImageUrl(docsDto.getFrontImageUrl());
        verificationDetail.setBackImageUrl(docsDto.getBackImageUrl());
        verificationDetail.setIsVerified(false); 
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        System.out.println("Thông tin đã được gửi để xác thực danh tính và sẽ được duyệt sớm nhất có thể");
        return verificationDetailRepository.save(verificationDetail);
    }

    @Transactional
    public VerificationDetail approveVerification(Integer verificationDetailId, Integer adminUserId) {
        VerificationDetail verificationDetail = verificationDetailRepository.findById(verificationDetailId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin xác thực mang ID này: " + verificationDetailId));

        User userToVerify = verificationDetail.getUser();
        if (userToVerify.getIsVerified() && verificationDetail.getIsVerified() != null && verificationDetail.getIsVerified()) {
            throw new IllegalStateException("User ID: " + userToVerify.getUserId() + " đã được xác thực và thông tin này đã được chấp nhận.");
        }
        if (verificationDetail.getSelfiePicUrl() == null || verificationDetail.getFrontImageUrl() == null || verificationDetail.getBackImageUrl() == null) { // Changed from getNationalId
             throw new IllegalStateException("Không thể chấp nhận xác thực danh tính cho User ID: " + userToVerify.getUserId() + " vì thông tin chưa được gửi đầy đủ trong thông tin này.");
        }

        userToVerify.setIsVerified(true); 
        userRepository.save(userToVerify);

        verificationDetail.setIsVerified(true); 
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        VerificationDetail savedDetail = verificationDetailRepository.save(verificationDetail);
        sendNotification(userToVerify, "Congratulations! Your identity verification has been approved.");
        return savedDetail;
    }

    @Transactional
    public VerificationDetail rejectVerification(Integer verificationDetailId, Integer adminUserId, String reason) {
        VerificationDetail verificationDetail = verificationDetailRepository.findById(verificationDetailId)
                .orElseThrow(() -> new EntityNotFoundException("VerificationDetail not found with ID: " + verificationDetailId));

        User userToUpdate = verificationDetail.getUser();
        if (verificationDetail.getSelfiePicUrl() == null || verificationDetail.getFrontImageUrl() == null || verificationDetail.getBackImageUrl() == null) { // Changed from getNationalId
             throw new IllegalStateException("Cannot reject verification for User ID: " + userToUpdate.getUserId() + " as documents appear incomplete in this detail.");
        }

        userToUpdate.setIsVerified(false); 
        userRepository.save(userToUpdate);

        verificationDetail.setIsVerified(false); 
        verificationDetail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        VerificationDetail savedDetail = verificationDetailRepository.save(verificationDetail);
        sendNotification(userToUpdate, "Hồ sơ xác thực của bạn đã bị từ chối. Lý do: " + reason + ". Vui lòng kiểm tra và gửi lại nếu cần thiết.");
        return savedDetail;
    }

    @Transactional(readOnly = true)
    public List<VerificationDetail> getPendingVerifications() {
        return verificationDetailRepository.findBySelfiePicUrlIsNotNullAndFrontImageUrlIsNotNullAndBackImageUrlIsNotNullAndIsVerifiedFalseAndUser_IsVerifiedFalse(); // Updated method call
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

    private void sendNotification(User user, String message) {
        if (user != null && message != null && !message.isEmpty()) {
            Notification notification = new Notification(user, message);
            notificationRepository.save(notification);
            System.out.println("Thông báo tới người dùng " + user.getUserId() + ": " + message);
        } else {
            System.out.println("Không thể gửi thông báo: user hoặc thông báo rỗng.");
        }
    }
}
