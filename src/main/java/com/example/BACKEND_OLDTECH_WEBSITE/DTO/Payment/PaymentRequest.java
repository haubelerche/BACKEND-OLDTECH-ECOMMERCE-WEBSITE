package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment;

import lombok.Data;

@Data
public class PaymentRequest {
    private String orderId;
    private long amount;
    private String orderInfo;
} 