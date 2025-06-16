package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class SellerRegisterResponse {
    private Integer sellerId;
    private String momoAccount;
    private Boolean isApproved;
    private AccountStatusEnum accountStatus;
    private Boolean businessStatus;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

