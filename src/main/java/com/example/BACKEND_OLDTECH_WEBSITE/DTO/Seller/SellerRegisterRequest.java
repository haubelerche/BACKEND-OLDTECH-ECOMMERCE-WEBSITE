package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller;

import lombok.Data;
import jakarta.validation.constraints.Size;

@Data
public class SellerRegisterRequest {
// đang muốn gộp khúc này với is approved, đc notion nhe
    private String account_status;

    @Size(min = 8, message = "Tài khoản Momo phải có ít nhất 8 ký tự")
    private String momoAccount;
}