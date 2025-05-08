package com.example.BACKEND_OLDTECH_WEBSITE.Controller;
/*
registerUser()
loginUser()
 */

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.LoginRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.RefreshTokenRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Security.JWTProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthCtrl {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTProvider tokenProvider;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
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

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Email hoặc mật khẩu không đúng");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đăng nhập thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (userService.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Email đã được sử dụng!");
            }

            if (userService.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
                return ResponseEntity.badRequest().body("Số điện thoại đã được đăng ký!");
            }

            User user = userService.createUser(registerRequest);
            return ResponseEntity.ok("Đăng ký thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đăng ký thất bại: " + e.getMessage());
        }
    }

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
}
