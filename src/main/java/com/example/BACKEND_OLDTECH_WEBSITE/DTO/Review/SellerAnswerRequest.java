package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerAnswerRequest {


    @NotBlank(message = "Trả lời bình luận không được để trống")
    @Size(max = 300, message = "Trả lời bình luận không vượt quá 300 ký tự")
    private String seller_response;

}
