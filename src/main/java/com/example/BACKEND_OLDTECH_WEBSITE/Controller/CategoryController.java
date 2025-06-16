package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Category.CategoryRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Category.CategoryResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Category;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Create a new category - only admin or seller can access
    @PostMapping("/createCategory")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('Seller')")
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest request) {
        try {
            Category category = new Category();
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            category.setAvatarUrl(request.getAvatarUrl());

            // Handle parent category if specified
            if (request.getParentCategoryId() != null) {
                Category parentCategory = categoryService.getCategoryById(request.getParentCategoryId().longValue());
                category.setParent(parentCategory);
            }

            Category created = categoryService.createCategory(category);

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không thể tạo danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Get all categories - public access
    @GetMapping("/getAllCategories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // Get top-level categories - public access
    @GetMapping("/getTopCategories")
    public ResponseEntity<List<Category>> getTopLevelCategories() {
        List<Category> topCategories = categoryService.getTopLevelCategories();
        return ResponseEntity.ok(topCategories);
    }

    // Get category by ID - public access
    @GetMapping("/getCategory/{categoryId}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long categoryId) {
        try {
            Category category = categoryService.getCategoryById(categoryId);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // Get subcategories of a specific category - public access
    @GetMapping("/getSubcategories/{categoryId}")
    public ResponseEntity<?> getSubcategories(@PathVariable Long categoryId) {
        try {
            List<Category> subcategories = categoryService.getSubcategories(categoryId);
            return ResponseEntity.ok(subcategories);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // Get entire category tree - public access
    @GetMapping("/getCategoryTree")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        List<Category> topCategories = categoryService.getTopLevelCategories();
        List<CategoryResponse> response = topCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Search categories by name - public access
    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String name) {
        List<Category> categories = categoryService.searchCategoriesByName(name);
        return ResponseEntity.ok(categories);
    }

    // Update a category - only admin or seller can access
    @PutMapping("/updateCategory/{categoryId}")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('Seller')")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.getCategoryById(categoryId);
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            category.setAvatarUrl(request.getAvatarUrl());

            // Handle parent category if specified
            if (request.getParentCategoryId() != null) {
                // Prevent circular references
                if (categoryId.equals(request.getParentCategoryId().longValue())) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Danh mục không thể là danh mục con của chính nó");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                Category parentCategory = categoryService.getCategoryById(request.getParentCategoryId().longValue());
                category.setParent(parentCategory);
            } else {
                category.setParent(null);
            }

            Category updated = categoryService.updateCategory(categoryId, category);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // Toggle category visibility - only admin or seller can access
    @PutMapping("/toggleVisibility/{categoryId}")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('Seller')")
    public ResponseEntity<?> toggleCategoryVisibility(@PathVariable Long categoryId) {
        try {
            Category updated = categoryService.toggleCategoryVisibility(categoryId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Trạng thái hiển thị của danh mục đã được cập nhật");
            response.put("visible", updated.isVisible());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // Delete a category - only admin can access
    @DeleteMapping("/deleteCategory/{categoryId}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Danh mục đã được xóa thành công.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Helper method to recursively build the category tree
    private CategoryResponse buildCategoryTree(Category category) {
        List<Category> childCategories = categoryService.getSubcategories(category.getId());
        List<CategoryResponse> children = childCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());

        return CategoryResponse.builder()
                .category_id(category.getId().intValue())
                .name(category.getName())
                .description(category.getDescription())
                .avatarUrl(category.getAvatarUrl())
                .parent_category_id(category.getParent() != null ? category.getParent().getId() : null)
                .isVisible(category.isVisible())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .subCategories(children)
                .build();
    }
}
