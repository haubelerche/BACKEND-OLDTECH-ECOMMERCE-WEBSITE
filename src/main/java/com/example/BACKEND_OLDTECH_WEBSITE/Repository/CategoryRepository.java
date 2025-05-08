package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentId(Long parentId);
    List<Category> findByParentIsNull();
}