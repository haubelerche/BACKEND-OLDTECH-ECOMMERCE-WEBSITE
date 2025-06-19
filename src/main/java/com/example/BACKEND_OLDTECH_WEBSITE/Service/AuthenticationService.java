package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Configuration.JWTProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.LoginRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Exception.AccountSuspendedException;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;

//TODO: DOMAIN IN HERE
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private final LoginAttemptService loginAttemptService;

    public Map<String, String> loginUser(@Valid LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String sessionId = null;
        Integer userId = null;
        
        // Get current HTTP request for IP tracking
        HttpServletRequest request = getCurrentRequest();
        
        try {
            User user = userService.findUserByEmail(email);
            userId = user != null ? user.getUserId() : null;

            // Check if account is suspended
            if (user != null && user.getAccountStatus() == AccountStatusEnum.Suspended) {
                logFailedAttempt(email, userId, request, "ACCOUNT_SUSPENDED", sessionId);
                
                LocalDateTime now = LocalDateTime.now();
                if (user.getSuspensionEndTime() != null) {
                    if (now.isBefore(user.getSuspensionEndTime())) {
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
                            email,
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate session ID for successful login
            sessionId = java.util.UUID.randomUUID().toString();

            // Log successful login attempt
            loginAttemptService.logLoginAttempt(email, userId, request, true, null, sessionId);

            // Update last login timestamp after successful authentication
            if (user != null) {
                user.setLastLogin(new java.sql.Timestamp(System.currentTimeMillis()));
                userService.updateUserForOAuth2(user);
            }

            String jwt = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(email);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", jwt);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("sessionId", sessionId);
            
            return response;
            
        } catch (BadCredentialsException e) {
            logFailedAttempt(email, userId, request, "INVALID_CREDENTIALS", sessionId);
            throw e;
        } catch (AuthenticationException e) {
            logFailedAttempt(email, userId, request, "AUTHENTICATION_FAILED", sessionId);
            throw e;
        } catch (AccountSuspendedException e) {
            // Already logged above
            throw e;
        } catch (Exception e) {
            logFailedAttempt(email, userId, request, "SYSTEM_ERROR", sessionId);
            throw e;
        }
    }

    private void logFailedAttempt(String email, Integer userId, HttpServletRequest request, 
                                String failureReason, String sessionId) {
        try {
            loginAttemptService.logLoginAttempt(email, userId, request, false, failureReason, sessionId);
        } catch (Exception e) {
            // Don't let logging failures affect the authentication process
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}

//TODO: dùng 1 email cố định and real to send message