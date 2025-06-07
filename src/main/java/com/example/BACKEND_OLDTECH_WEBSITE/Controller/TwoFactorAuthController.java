package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TwoFactorSetupResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TwoFactorPasswordResetRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TwoFactorPasswordResetSimpleRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.TwoFactorCodeVerifyRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.TwoFactorAuthService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth/2fa")
@CrossOrigin(origins = "*")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final UserService userService;

    // Store verification tokens with email as the key and token as the value
    private final Map<String, String> verificationTokens = new ConcurrentHashMap<>();
    // Token expiry - default 10 minutes
    private static final long TOKEN_EXPIRY_MINUTES = 10;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    public TwoFactorAuthController(TwoFactorAuthService twoFactorAuthService, UserService userService) {
        this.twoFactorAuthService = twoFactorAuthService;
        this.userService = userService;
    }

    // cho người định login mà quên mật khẩu, dùng 2FA để lấy lại mật khẩu
    @GetMapping("/recover-qr")
    public ResponseEntity<?> recoverTwoFactorQR(@RequestParam String email) {
        try {
            User user;
            try {
                user = userService.findUserByEmail(email);
            } catch (UsernameNotFoundException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy tài khoản với email này");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            boolean is2FAEnabled = twoFactorAuthService.is2FAEnabled(user);
            if (!is2FAEnabled) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Tài khoản này chưa thiết lập xác thực hai lớp");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            TwoFactorSetupResponse qrResponse = twoFactorAuthService.regenerateQRCode(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "QR code đã được tạo lại thành công");
            response.put("qrCodeDataUrl", qrResponse.getQrCodeDataUrl());
            response.put("secretKey", qrResponse.getSecretKey());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi khôi phục QR code: " + e.getMessage());
            error.put("error", "RECOVERY_FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


// STEP 2: Reset password using token from previous step
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody TwoFactorPasswordResetRequest request) {
        try {
            // Validate the token
            String storedToken = verificationTokens.get(request.getEmail());
            if (storedToken == null || !storedToken.equals(request.getToken())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Token không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Find the user
            User user;
            try {
                user = userService.findUserByEmail(request.getEmail());
            } catch (UsernameNotFoundException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy tài khoản với email này");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Change the password
            userService.changeUserPassword(user, request.getNewPassword());

            // Remove the token as it has been used
            verificationTokens.remove(request.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mật khẩu đã được cập nhật thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi đặt lại mật khẩu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }   
    

//ĐỂ CHECK TRẠNG THÁI XÁC THỰC HAI LỚP XEM họ bật xác thực hai lớp hay chưa
    @GetMapping("/status")
    public ResponseEntity<?> getTwoFactorStatus(@RequestParam String email) {
        try {
            User user = userService.findUserByEmail(email);
            boolean isEnabled = twoFactorAuthService.is2FAEnabled(user);

            Map<String, Object> response = new HashMap<>();
            response.put("enabled", isEnabled);
            response.put("email", email);

            if (isEnabled) {
                response.put("recoveryMethod", "2fa");
                response.put("message", "Bạn đã thiết lập xác thực hai lớp. Vui lòng sử dụng mã từ ứng dụng Google Authenticator để đặt lại mật khẩu.");
            } else {
                response.put("recoveryMethod", "email");
                response.put("message", "Bạn chưa thiết lập xác thực hai lớp. Vui lòng sử dụng email để đặt lại mật khẩu.");
            }

            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Không tìm thấy tài khoản với email này");
            error.put("recoveryMethod", "email"); // Default to email recovery
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi kiểm tra trạng thái xác thực hai lớp: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

// STEP 1: Verify 2FA code and get token
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyTwoFactorCode(@RequestBody TwoFactorCodeVerifyRequest request) {
        try {
            User user;
            try {
                user = userService.findUserByEmail(request.getEmail());
            } catch (UsernameNotFoundException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy tài khoản với email này");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            boolean is2FAEnabled = twoFactorAuthService.is2FAEnabled(user);
            if (!is2FAEnabled) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Xác thực hai lớp chưa được thiết lập cho tài khoản này");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Verify the provided 2FA code
            boolean isCodeValid = twoFactorAuthService.verifyCode(request.getEmail(), request.getCode());
            if (!isCodeValid) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Mã xác thực không hợp lệ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Code is valid - generate a verification token
            String token = UUID.randomUUID().toString();
            verificationTokens.put(request.getEmail(), token);

            // Set token to expire after TOKEN_EXPIRY_MINUTES
            scheduler.schedule(() -> {
                verificationTokens.remove(request.getEmail());
            }, TOKEN_EXPIRY_MINUTES, TimeUnit.MINUTES);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mã xác thực hợp lệ. Vui lòng đặt lại mật khẩu của bạn.");
            response.put("token", token);
            response.put("email", request.getEmail());
            response.put("expiresInMinutes", TOKEN_EXPIRY_MINUTES);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi xác thực mã: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Simplified password reset with 2FA - Single step process
     * This is more user-friendly than the two-step token process
     */
    @PostMapping("/reset-password-simple")
    public ResponseEntity<?> resetPasswordSimple(@RequestBody TwoFactorPasswordResetSimpleRequest request) {
        try {
            User user;
            try {
                user = userService.findUserByEmail(request.getEmail());
            } catch (UsernameNotFoundException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy tài khoản với email này");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Check if user has 2FA enabled
            boolean is2FAEnabled = twoFactorAuthService.is2FAEnabled(user);
            if (!is2FAEnabled) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Xác thực hai lớp chưa được thiết lập cho tài khoản này");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Verify the provided 2FA code
            boolean isCodeValid = twoFactorAuthService.verifyCode(request.getEmail(), request.getCode());
            if (!isCodeValid) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Mã xác thực không hợp lệ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Code is valid - reset the password directly
            userService.changeUserPassword(user, request.getNewPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mật khẩu đã được đặt lại thành công");
            response.put("email", request.getEmail());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi đặt lại mật khẩu: " + e.getMessage());
            error.put("error", "RESET_FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}
