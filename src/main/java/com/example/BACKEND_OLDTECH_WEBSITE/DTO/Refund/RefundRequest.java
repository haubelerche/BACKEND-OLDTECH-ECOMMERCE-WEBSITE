package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Refund;

import lombok.Data;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

@Data
public class RefundRequest {

    private Integer orderId;
     private Integer sellerId;
    @NotBlank(message = "Lý do không được để trống")
    @Size(min = 200, message = "Giải trình trên 200 ký tự")
    private String reason;
}