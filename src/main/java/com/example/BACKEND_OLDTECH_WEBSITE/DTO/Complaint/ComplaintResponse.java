package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Complaint;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComplaintResponse {
    private Integer complaintId;
    private Integer complainantId;
    private Integer respondentId;
    private Integer orderId;
    private String reason;
    @NotBlank(message = "Cập nhật trạng thái không được để trống")
    private String status;
    @NotBlank(message = "Ghi chú giải quyết không được để trống")
    private String adminResponse;
}
