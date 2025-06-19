package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository đặc biệt cho phân tích sản phẩm
 */
@Repository
public interface ETLProductRepository extends JpaRepository<Product, Integer> {

    /**
     * Đếm sản phẩm hoạt động theo người bán cho ETL metrics
     */
    @Query("SELECT COUNT(p.productId) FROM Product p WHERE p.sellerId = :sellerId AND p.status = 'APPROVED'")
    Integer countActiveProductsBySellerId(@Param("sellerId") Integer sellerId);    /**
     * Lấy thống kê hiệu suất sản phẩm
     */
    @Query("SELECT p.productId, p.name, c.name, 0 " +
           "FROM Product p " +
           "LEFT JOIN Category c ON c.id = p.categoryId " +
           "WHERE p.status = 'APPROVED'")
    List<Object[]> getProductPerformanceData();
}
