package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AuthProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import org.springframework.format.annotation.DateTimeFormat;



//import com.fasterxml.jackson.annotation.JsonFormat; if any1 ask how to change the day format
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
    private Integer userId;

    @NotBlank(message = "Bắt buộc nhập email")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail.com$", message = "Định dạng email không hợp lệ")
    @Column(unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @NotBlank(message = "Bắt buộc nhập số điện thoại")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Sai định dạng số điện thoại")
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @NotBlank(message = "Bắt buộc nhập tên")
    @Column(name = "first_name")
    private String firstName;


    @Column(name = "selfie_pic_url")
    private String selfiePicUrl;

    @Column(name = "front_image_url")
    private String frontImageUrl;

    @Column(name = "back_image_url")
    private String backImageUrl;

    @NotBlank(message = "Bắt buộc nhập họ")
    @Column(name = "last_name")
    private String lastName;


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dob")
    private Date dob;

    @Column(name = "avatar_url", columnDefinition = "LONGTEXT")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    @Builder.Default
    private AccountStatusEnum accountStatus = AccountStatusEnum.Active;

    @Column(name = "refund_momo_account")
    private String refundMomoAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthProvider authProvider;

    @Column(name = "auth_provider_id")
    private String authProviderId;

    @Column(name = "auth_provider_token", columnDefinition = "LONGTEXT")
    private String authProviderToken;

    @Column(name = "auth_provider_refresh_token", columnDefinition = "LONGTEXT")
    private String authProviderRefreshToken;

    @Column(name = "auth_provider_token_expires")
    private Timestamp authProviderTokenExpires;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "last_login", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Builder.Default
    private Timestamp lastLogin = new Timestamp(System.currentTimeMillis());

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "suspension_end_time")
    private LocalDateTime suspensionEndTime;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    // Helper method for 2FA status checking
    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled != null && twoFactorEnabled;
    }

    @Column(name= "fixed_location")
    private String fixedLocation;
}
