package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminComplaintResponse {
    
    @NotNull(message = "Trạng thái không được để trống")
    private ComplaintStatus status;
    
    @NotBlank(message = "Phản hồi của admin không được để trống")
    @Size(max = 2000, message = "Phản hồi không được vượt quá 2000 ký tự")
    private String adminResponse;
}
