package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.AgeRestriction;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniqueEmail;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniquePhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Tên không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @UniqueEmail(message = "Email đã tồn tại")  // cái này sẽ check ở user repo
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số")
    @UniquePhoneNumber(message = "Số điện thoại đã tồn tại")
    private String phoneNumber;

    @NotBlank(message = "Họ không được để trống")
    private String firstName;
    private String lastName;

    private RoleEnum role;

    @NotBlank(message = "Ngày sinh không được để trống")
    @AgeRestriction(message = "Yêu cầu trên 18 tuổi để đăng ký tài khoản")
    private Date dob;

    private String avatarUrl;
}
