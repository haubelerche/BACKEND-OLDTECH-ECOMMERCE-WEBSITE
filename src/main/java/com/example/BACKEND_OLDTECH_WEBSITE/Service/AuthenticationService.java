package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Configuration.JWTProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.LoginRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Exception.AccountSuspendedException;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.PasswordResetToken;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JWTProvider tokenProvider;
    private final UserService userService;

    public Map<String, String> loginUser(@Valid LoginRequest loginRequest) {
        // Check if the user is suspended before attempting authentication
        User user = userService.findUserByEmail(loginRequest.getEmail());

        // Check for suspension status
        if (user.getAccountStatus() == AccountStatusEnum.Suspended) {
            // If suspended, check if it's temporary or permanent
            LocalDateTime now = LocalDateTime.now();
            if (user.getSuspensionEndTime() != null) {
                if (now.isBefore(user.getSuspensionEndTime())) {
                    // Calculate remaining time
                    Duration duration = Duration.between(now, user.getSuspensionEndTime());
                    long hours = duration.toHours();
                    long minutes = duration.toMinutesPart();

                    String timeRemaining = hours + " giờ " + minutes + " phút";
                    String reason = user.getSuspensionReason() != null ? user.getSuspensionReason() : "Vi phạm quy định của nền tảng";

                    throw new AccountSuspendedException(
                            "Tài khoản của bạn đã bị tạm khóa. Thời gian còn lại: " +
                                    timeRemaining + ". Lý do: " + reason);
                } else {
                    // Suspension period is over, reactivate the account
                    user.setAccountStatus(AccountStatusEnum.Active);
                    user.setSuspensionEndTime(null);
                    user.setSuspensionReason(null);
                    userService.updateUserForOAuth2(user);
                }
            } else {
                // Permanent suspension
                String reason = user.getSuspensionReason() != null ? user.getSuspensionReason() : "Vi phạm quy định của nền tảng";
                throw new AccountSuspendedException(
                        "Tài khoản của bạn đã bị tạm khóa vĩnh viễn. Lý do: " + reason);
            }
        }

        // Proceed with normal authentication if not suspended
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(loginRequest.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", jwt);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        return response;
    }

    public User registerUser(@Valid RegisterRequest registerRequest) {
        return userService.createUser(registerRequest);
    }

    public String handleForgotPassword(String email) {
        User user = userService.findUserByEmail(email);
        String token = userService.createPasswordResetTokenForUser(user);

        System.out.println("Password Reset Token for " + user.getEmail() + ": " + token);
        System.out.println("Reset URL (simulation): http://yourfrontenddomain.com/reset-password?token=" + token);

        return "Nếu email của bạn tồn tại trong hệ thống, một liên kết đặt lại mật khẩu đã được gửi.";
    }

    public String handleResetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = userService.getPasswordResetToken(token);

        if (resetToken == null) {
            throw new IllegalArgumentException("Token đặt lại mật khẩu không hợp lệ.");
        }
        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token đặt lại mật khẩu đã hết hạn.");
        }

        User user = resetToken.getUser();
        userService.changeUserPassword(user, newPassword);

        return "Mật khẩu của bạn đã được đặt lại thành công.";
    }
}

