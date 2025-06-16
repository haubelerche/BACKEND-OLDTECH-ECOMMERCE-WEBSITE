package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Review;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
public class ReviewRequest {

    @NotNull(message = "OrderId không được để trống")
    private Integer orderId;

    @NotNull(message = "ProductId không được để trống")
    private Integer productId;

    @NotNull(message = "Đánh giá sao không được để trống")
    @Min(value = 1, message = "Đánh giá phải có giá trị từ 1 đến 5 sao")
    @Max(value = 5, message = "Đánh giá phải có giá trị từ 1 đến 5 sao")
    private Integer rating;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    @Size(max = 300, message = "Nội dung đánh giá không vượt quá 300 ký tự")
    private String review;
}

