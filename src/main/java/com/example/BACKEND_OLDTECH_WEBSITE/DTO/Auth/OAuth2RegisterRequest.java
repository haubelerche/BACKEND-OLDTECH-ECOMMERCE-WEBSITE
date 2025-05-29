package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;

import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniquePhoneNumber;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.AgeRestriction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuth2RegisterRequest {
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số")
    @UniquePhoneNumber(message = "Số điện thoại đã tồn tại")
    private String phoneNumber;

    @NotNull(message = "Ngày sinh không được để trống")
    @AgeRestriction(message = "Yêu cầu trên 18 tuổi để đăng ký tài khoản")
    private Date dob;

    private String refundMomoAccount;

    // Optional password for additional security
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    // These might be pre-filled from OAuth provider but can be edited
    private String firstName;
    private String lastName;

    private Boolean isSellerRequest;
}

