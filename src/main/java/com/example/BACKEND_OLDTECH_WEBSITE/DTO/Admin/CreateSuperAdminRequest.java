package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateSuperAdminRequest {

    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^managerotech[A-Za-z0-9._%+-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Đây không phải định dạng Email của tổng quản lý")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String password;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số")
    private String phoneNumber;

    @NotBlank(message = "Họ không được để trống")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    private String lastName;
} 