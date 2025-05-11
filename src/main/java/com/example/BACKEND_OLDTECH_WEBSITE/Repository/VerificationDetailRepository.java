package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.VerificationDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationDetailRepository extends JpaRepository<VerificationDetail, Integer> {
    Optional<VerificationDetail> findByUser(User user);
    List<VerificationDetail> findBySelfiePicUrlIsNotNullAndFrontImageUrlIsNotNullAndBackImageUrlIsNotNullAndIsVerifiedFalseAndUser_IsVerifiedFalse();
}