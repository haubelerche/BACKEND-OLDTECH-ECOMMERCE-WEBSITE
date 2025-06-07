package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TwoFactorSetupResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Service
public class TwoFactorAuthService {

    private static final Logger logger = LoggerFactory.getLogger(TwoFactorAuthService.class);

    @Value("${app.2fa.issuer:OldTech}")
    private String issuer;

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public TwoFactorAuthService(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Generate a new secret key for 2FA and store it for the user
     * Returns a QR code image data URL and the raw secret key to be scanned by Google Authenticator
     */
    @Transactional
    public TwoFactorSetupResponse generateTwoFactorSecret(User user) throws QrGenerationException {
        // Generate a 2FA secret
        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        String secret = secretGenerator.generate();

        // Log the secret to verify it matches the expected format (32 characters)
        logger.info("Generated 2FA secret key for {}: {} (length: {})",
                user.getEmail(), secret, secret.length());

        // Store the secret in the user record
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(false);  // Will be enabled after verification
        userRepository.save(user);

        // Generate the QR code
        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator qrGenerator = new ZxingPngQrGenerator();
        byte[] qrImage = qrGenerator.generate(qrData);

        // Create and populate the response
        TwoFactorSetupResponse response = new TwoFactorSetupResponse();
        response.setQrCodeDataUrl("data:image/png;base64," + Base64.getEncoder().encodeToString(qrImage));
        response.setSecretKey(secret);
        response.setMessage("Vui lòng quét mã QR này bằng ứng dụng Google Authenticator hoặc nhập mã bí mật thủ công");

        return response;
    }

    /**
     * Check if the provided code is valid for the given user's 2FA setup
     */
    public boolean verifyCode(String email, String code) {
        User user = userService.findUserByEmail(email);

        if (user == null || user.getTwoFactorSecret() == null) {
            return false;
        }

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        boolean codeIsValid = verifier.isValidCode(user.getTwoFactorSecret(), code);

        // If this is the first verification, enable 2FA for the user
        if (codeIsValid && !user.isTwoFactorEnabled()) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
        }

        return codeIsValid;
    }

    /**
     * Check if 2FA is enabled for a user
     */
    public boolean is2FAEnabled(User user) {
        return user != null && user.isTwoFactorEnabled() && user.getTwoFactorSecret() != null;
    }

    /**
     * Disable 2FA for a user (used for password resets)
     */
    @Transactional
    public void disable2FA(User user) {
        if (user != null) {
            user.setTwoFactorSecret(null);
            user.setTwoFactorEnabled(false);
            userRepository.save(user);
        }
    }

    /**
     * Regenerate QR code using existing secret (for password recovery scenarios)
     * This doesn't create a new secret, just regenerates the QR code
     */
    @Transactional(readOnly = true)
    public TwoFactorSetupResponse regenerateQRCode(User user) throws QrGenerationException {
        if (user.getTwoFactorSecret() == null || user.getTwoFactorSecret().trim().isEmpty()) {
            throw new IllegalStateException("User does not have a 2FA secret configured");
        }

        String existingSecret = user.getTwoFactorSecret();
        
        logger.info("Regenerating QR code for user: {} using existing secret", user.getEmail());

        // Generate the QR code using the existing secret
        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(existingSecret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator qrGenerator = new ZxingPngQrGenerator();
        byte[] qrImage = qrGenerator.generate(qrData);

        // Create and populate the response
        TwoFactorSetupResponse response = new TwoFactorSetupResponse();
        response.setQrCodeDataUrl("data:image/png;base64," + Base64.getEncoder().encodeToString(qrImage));
        response.setSecretKey(existingSecret);
        response.setMessage("QR code đã được tạo lại từ secret key hiện có");

        return response;
    }
}
