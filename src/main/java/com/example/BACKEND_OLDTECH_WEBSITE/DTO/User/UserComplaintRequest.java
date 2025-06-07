package com.example.BACKEND_OLDTECH_WEBSITE.DTO.User;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class UserComplaintRequest {
    
    @NotBlank(message = "Nội dung khiếu nại không được để trống")
    @Size(min = 10, max = 1000, message = "Nội dung khiếu nại phải có độ dài từ 10 đến 1000 ký tự")
    private String complaint;
    
    @Size(max = 200, message = "Tiêu đề khiếu nại không được vượt quá 200 ký tự")
    private String subject; // Optional subject for the complaint
    
    // Constructors
    public UserComplaintRequest() {}
    
    public UserComplaintRequest(String complaint) {
        this.complaint = complaint;
    }
    
    public UserComplaintRequest(String complaint, String subject) {
        this.complaint = complaint;
        this.subject = subject;
    }
}
