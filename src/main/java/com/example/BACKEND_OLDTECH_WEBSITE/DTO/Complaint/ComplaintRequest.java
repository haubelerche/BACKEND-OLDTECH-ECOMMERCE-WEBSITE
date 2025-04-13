package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ComplaintRequest {
    @NotBlank(message = "Lý do không được để trống")
    private String reason;
    @NotBlank
    private Integer reportedUserId;
    @NotBlank
    private Integer orderId;
}