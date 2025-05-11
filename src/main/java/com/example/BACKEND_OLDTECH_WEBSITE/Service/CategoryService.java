package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Category;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public Category createCategory(Category category) {
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
        
        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    public List<Category> getSubcategories(Long parentId) {
     
        if (!categoryRepository.existsById(parentId)) {
            throw new RuntimeException("Danh mục cha không tồn tại với ID: " + parentId);
        }
        return categoryRepository.findByParentId(parentId);
    }

    public List<Category> getTopLevelCategories() {
        return categoryRepository.findByParentIsNull();
    }
}