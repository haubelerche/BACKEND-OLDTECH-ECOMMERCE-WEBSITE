package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SellerVerifyRequest {
    private Boolean isApproved; // True to approve, false to reject
    private String reason; // Optional reason for rejection
}
