package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Category;
//GET
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Integer category_id;
    private String name;
    private String slug;
    private Long parent_category_id;
    private boolean isVisible;
    private List<CategoryResponse> subCategories;
}

//slug is a help to create a friendly url. easier to read from url