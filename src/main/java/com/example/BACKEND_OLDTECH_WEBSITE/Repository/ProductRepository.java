package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    List<Product> findBySellerId(Integer sellerId);
    List<Product> findByStatus(ProductStatusEnum status);
    Page<Product> findByIsVisibleTrueAndIsApprovedTrueAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name, String description, Pageable pageable);
    Page<Product> findByIsVisibleTrueAndIsApprovedTrue(Pageable pageable);

    @Query(value = "SELECT * FROM product WHERE is_visible = true AND is_approved = true ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<Product> findRandomVisibleApprovedProducts(@Param("count") int count);
}