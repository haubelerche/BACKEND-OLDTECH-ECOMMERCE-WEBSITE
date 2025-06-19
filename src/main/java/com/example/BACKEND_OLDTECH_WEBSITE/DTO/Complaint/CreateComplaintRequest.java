package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateComplaintRequest {
    
    // Optional - for order-related complaints
    private Integer orderId;
    
    // Optional - for complaints against specific users/sellers
    private Integer respondentId;
    
    @NotBlank(message = "Lý do khiếu nại không được để trống")
    @Size(max = 2000, message = "Lý do khiếu nại không được vượt quá 2000 ký tự")
    private String reason;
}
