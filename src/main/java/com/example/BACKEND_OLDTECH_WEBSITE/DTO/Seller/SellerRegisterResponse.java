package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class SellerRegisterResponse {
    private Integer sellerId;
    private String momoAccount;
    private String account_status;
    private Timestamp created_at;
    private Timestamp updated_at;
}