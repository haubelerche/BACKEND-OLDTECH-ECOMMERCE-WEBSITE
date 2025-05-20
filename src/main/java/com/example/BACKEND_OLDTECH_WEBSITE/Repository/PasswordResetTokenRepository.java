package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.PasswordResetToken;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByUser(User user); // Helpful for cleaning up old tokens
} 