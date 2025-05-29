package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller;

import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

@Data
public class SellerRegisterRequest {
    @NotBlank(message = "Tài khoản Momo không được để trống")
    @Size(min = 8, message = "Tài khoản Momo phải có ít nhất 8 ký tự")
    private String momoAccount;
    
    @NotNull(message = "ID người dùng không được để trống")
    private Integer userId;

    @AssertTrue(message = "Bạn phải đồng ý với chính sách người bán của O' Tech để tiếp tục")
    private Boolean policyAgreement;
}

