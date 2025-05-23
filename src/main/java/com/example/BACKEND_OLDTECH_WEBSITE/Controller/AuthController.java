package com.example.BACKEND_OLDTECH_WEBSITE.Controller;
//90%
//TODO:ASK Long ABOUT DOMAIN
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.LoginRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.RefreshTokenRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.ForgotPasswordRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.ResetPasswordRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Configuration.JWTProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.Exception.AccountSuspendedException;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")

@CrossOrigin(origins = "*",maxAge = 3600)
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

   
    private final JWTProvider tokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;



//ĐĂNG NHẬP
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // First check if the user exists and is suspended before attempting authentication
            try {
                // Get the user first to check if it exists
                userService.findUserByEmail(loginRequest.getEmail());
            } catch (UsernameNotFoundException e) {

                return ResponseEntity.badRequest().body("Email hoặc mật khẩu không đúng");
            }

            try {
                Map<String, String> tokens = authenticationService.loginUser(loginRequest);
                return ResponseEntity.ok(tokens);
            } catch (AccountSuspendedException e) {
                // Explicitly handle the suspension exception
                return ResponseEntity.status(403).body(e.getMessage());
            }
        } catch (BadCredentialsException e) {
            // Bad credentials exception
            return ResponseEntity.badRequest().body("Email hoặc mật khẩu không đúng");
        } catch (Exception e) {
            // Check if the cause is an AccountSuspendedException
            Throwable cause = e.getCause();
            if (cause instanceof AccountSuspendedException) {
                return ResponseEntity.status(403).body(cause.getMessage());
            }
            // Other generic errors
            return ResponseEntity.badRequest().body("Đăng nhập thất bại: " + e.getMessage());
        }
    }

//TODO: HOW TO KNOW IF A PERSON TRY TO CREATE A NEW ACCOUNT WHEN HER OLD ONE IS SUSPENDED

//ĐĂNG KÝ
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
    System.out.println("Register endpoint called!");
        try {
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã được sử dụng!");
        }

        if (userService.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            return ResponseEntity.badRequest().body("Số điện thoại đã được đăng ký!");
        }

        userService.createUser(registerRequest);
        return ResponseEntity.ok("Đăng ký thành công!");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Đăng ký thất bại: " + e.getMessage());
    }
}




//LÀM MỚI TOKEN
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();
            
            if (!tokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.badRequest().body("Refresh token không hợp lệ!");
            }

            String username = tokenProvider.getUsernameFromToken(refreshToken);
            String newAccessToken = tokenProvider.generateTokenFromUsername(username);
            String newRefreshToken = tokenProvider.generateRefreshToken(username);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", newRefreshToken);
            response.put("tokenType", "Bearer");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Làm mới token thất bại: " + e.getMessage());
        }
    }



//QUÊN MẬT KHẨU
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        try {
            String result = authenticationService.handleForgotPassword(forgotPasswordRequest.getEmail());
            return ResponseEntity.ok(result);
        } catch (UsernameNotFoundException e) {
            // Even if user not found, return a generic message to prevent email enumeration
            return ResponseEntity.ok("Nếu email của bạn tồn tại trong hệ thống, một liên kết đặt lại mật khẩu đã được gửi.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi yêu cầu đặt lại mật khẩu: " + e.getMessage());
        }
    }




//ĐẶT LẠI MẬT KHẨU
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            String result = authenticationService.handleResetPassword(
                resetPasswordRequest.getToken(),
                resetPasswordRequest.getNewPassword()
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi đặt lại mật khẩu: " + e.getMessage());
        }
    }



//ĐĂNG XUẤT
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // For stateless JWT, just return OK. Client should delete the token.
        return ResponseEntity.ok("Đăng xuất thành công!");
    }









}
