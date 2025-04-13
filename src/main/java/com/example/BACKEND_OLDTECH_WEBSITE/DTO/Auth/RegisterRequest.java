package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.AgeRestriction;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniqueEmail;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniquePhoneNumber;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniqueUsername;
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

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    @UniqueEmail(message = "Email đã tồn tại")  // cái này sẽ check ở user repo
    private String email;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 6, max = 20, message = "Username phải từ 6 đến 20 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username phải chứa cả chữ cái và số hoặc ký tự đặc biệt")
    @UniqueUsername(message = "Username đã tồn tại")
    private String username;


    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 10, max = 20, message = "Mật khẩu phải từ 10 đến 20 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Password phải chứa cả chữ cái và số hoặc ký tự đặc biệt")
    private String password;


    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(min = 10, max = 10, message = "Số điện thoại phải gồm 10 số")
    @Pattern(regexp = "^[0-9]+$", message = "Số điện thoại chỉ được chứa chữ số")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số")
    @UniquePhoneNumber(message = "Số điện thoại đã tồn tại")
    private String phone_number;

@NotBlank(message = "Họ không được để trống")
    private String first_name;
    private String last_name;


    @NotBlank(message = "Ngày sinh không được để trống")
    @AgeRestriction(message = "Yêu cầu trên 18 tuổi để đăng ký tài khoản")
    private Date dob;

    private String avatarUrl;
}
