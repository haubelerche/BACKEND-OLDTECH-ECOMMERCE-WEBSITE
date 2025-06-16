package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Category;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Integer category_id;
    private String name;
    private String description;
    private String avatarUrl;
    private Long parent_category_id;
    private boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryResponse> subCategories;
}

