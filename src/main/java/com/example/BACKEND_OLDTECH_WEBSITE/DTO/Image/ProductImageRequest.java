
package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Image;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductImageRequest {
    private Integer imageId;
    @NotBlank(message = "URL không được để trống")

    private String imageUrl;
    @NotBlank(message = "Ảnh chính không được để trống")
    private Boolean isThumbnail;
}