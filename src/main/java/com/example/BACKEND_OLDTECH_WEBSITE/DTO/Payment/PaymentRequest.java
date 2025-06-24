package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Data
public class PaymentRequest {
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @Positive(message = "Amount must be greater than 0")
    private long amount;
    
    @Size(max = 500, message = "Order info must not exceed 500 characters")
    private String orderInfo;
}