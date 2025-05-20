package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Product;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

@Data
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 100, message = "Mô tả không được ít hơn 100 ký tự")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;
}