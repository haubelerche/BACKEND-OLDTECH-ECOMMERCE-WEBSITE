package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification;

import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

@Data
public class VerificationRequest {

    @NotBlank(message = "URL ảnh selfie không được để trống")
    @Size(max = 2048, message = "URL ảnh selfie không được vượt quá 2048 ký tự")
    private String selfiePicUrl;

    @NotBlank(message = "URL ảnh mặt trước không được để trống")
    @Size(max = 2048, message = "URL ảnh mặt trước không được vượt quá 2048 ký tự")
    private String frontImageUrl;

    @NotBlank(message = "URL ảnh mặt sau không được để trống")
    @Size(max = 2048, message = "URL ảnh mặt sau không được vượt quá 2048 ký tự")
    private String backImageUrl;
}