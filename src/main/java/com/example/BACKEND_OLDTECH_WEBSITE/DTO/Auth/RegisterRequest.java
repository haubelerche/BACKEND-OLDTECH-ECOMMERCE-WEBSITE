package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.AgeRestriction;
import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniqueEmail;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail.com$", message = "Định dạng email không hợp lệ")
    @UniqueEmail(message = "Email đã tồn tại")  // cái này sẽ check ở user repository
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số")
    private String phoneNumber;

    @NotBlank(message = "Họ không được để trống")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    private String lastName;


    @NotNull(message = "Ngày sinh không được để trống")
    @AgeRestriction(message = "Yêu cầu trên 18 tuổi để đăng ký tài khoản")
    private Date dob;

    private String avatarUrl;

}
