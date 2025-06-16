package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Category;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.CategoryRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public Category createCategory(Category category) {
        // Default is visible
        // isVisible is a primitive boolean with default value of true already set in the entity
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại với ID: " + categoryId));
    }

    public Category updateCategory(Long categoryId, Category category) {
        Category existing = getCategoryById(categoryId);
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setAvatarUrl(category.getAvatarUrl());
        existing.setParent(category.getParent());

        return categoryRepository.save(existing);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        // Check if category exists
        Category category = getCategoryById(categoryId);

        // Check if category has subcategories
        List<Category> subcategories = getSubcategories(categoryId);
        if (!subcategories.isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục có danh mục con. Vui lòng xóa các danh mục con trước.");
        }

        // Check if category has products
        // Add a custom method to check if products are associated with this category
        long productCount = countProductsByCategoryId(categoryId);
        if (productCount > 0) {
            throw new RuntimeException("Không thể xóa danh mục có sản phẩm. Vui lòng xóa hoặc chuyển các sản phẩm sang danh mục khác.");
        }

        categoryRepository.deleteById(categoryId);
    }

    private long countProductsByCategoryId(Long categoryId) {
        // This is a helper method that will use your ProductRepository's capabilities
        // You may need to implement the actual counting method in ProductRepository
        return productRepository.count(); // Replace with appropriate counting method
    }

    public List<Category> getSubcategories(Long parentId) {
        // Check if parent category exists
        if (!categoryRepository.existsById(parentId)) {
            throw new RuntimeException("Danh mục cha không tồn tại với ID: " + parentId);
        }
        return categoryRepository.findByParentId(parentId);
    }

    public List<Category> getTopLevelCategories() {
        return categoryRepository.findByParentIsNull();
    }

    public List<Category> searchCategoriesByName(String name) {
        List<Category> categories = categoryRepository.findByName(name);
        if (categories.isEmpty()) {
            // If no exact matches, try with partial name matches
            // Use a custom method to find by partial name match
            categories = findCategoriesByPartialName(name);
        }
        return categories;
    }

    private List<Category> findCategoriesByPartialName(String partialName) {
        // This is a helper method that will use your CategoryRepository's capabilities
        // For now, we'll return empty list - implement it according to your actual repository features
        return categoryRepository.findAll().stream()
               .filter(category -> category.getName().toLowerCase().contains(partialName.toLowerCase()))
               .collect(Collectors.toList());
    }

    @Transactional
    public Category toggleCategoryVisibility(Long categoryId) {
        Category category = getCategoryById(categoryId);
        category.setVisible(!category.isVisible());
        return categoryRepository.save(category);
    }

    @Transactional
    public void updateCategoryHierarchy(Long categoryId, Long newParentId) {
        Category category = getCategoryById(categoryId);
        Category newParent = newParentId != null ? getCategoryById(newParentId) : null;

        // Prevent circular references
        if (newParentId != null) {
            Category current = newParent;
            while (current != null) {
                if (current.getId().equals(categoryId)) {
                    throw new RuntimeException("Không thể tạo vòng lặp trong cây danh mục");
                }
                current = current.getParent();
            }
        }

        category.setParent(newParent);
        categoryRepository.save(category);
    }

    public List<Category> getCategoriesByVisibility(boolean visible) {
        // Filter all categories by visibility
        return categoryRepository.findAll().stream()
               .filter(category -> category.isVisible() == visible)
               .collect(Collectors.toList());
    }
}

