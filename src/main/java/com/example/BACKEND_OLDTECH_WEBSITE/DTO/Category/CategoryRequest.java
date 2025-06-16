package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Category;

import lombok.*;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    private String name;
    private String description;
    private String avatarUrl;
    private Integer parentCategoryId;
}

