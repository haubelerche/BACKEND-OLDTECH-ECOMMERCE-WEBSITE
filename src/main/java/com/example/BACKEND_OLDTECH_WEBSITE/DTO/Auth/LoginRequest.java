package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;

import com.example.BACKEND_OLDTECH_WEBSITE.Validation.Interface.UniqueUsername;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 6, max = 20, message = "Username phải từ 6 đến 20 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username phải chứa cả chữ cái và số hoặc ký tự đặc biệt")
    @UniqueUsername(message = "Username đã tồn tại")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 10, max = 20, message = "Mật khẩu phải từ 10 đến 20 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Password phải chứa cả chữ cái và số hoặc ký tự đặc biệt")
    private String password;

}
