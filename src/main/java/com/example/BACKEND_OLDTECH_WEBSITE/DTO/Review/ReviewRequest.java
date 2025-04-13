package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Review;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class ReviewRequest {

    @NotBlank(message = "Đánh giá không được để trống")

    private Integer rating;

    @NotBlank(message = "Bình luận không được để trống")
    @Size(max = 300, message = "Bình luận không vượt quá 300 ký tự")
    private String comment;
}