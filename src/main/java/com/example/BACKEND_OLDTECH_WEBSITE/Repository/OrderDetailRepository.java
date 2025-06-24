package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    List<OrderDetail> findByProduct_SellerId(Integer sellerId);
    List<OrderDetail> findByOrder_OrderId(Integer orderId);
}


    /**
     * Calculate total quantity of products sold by seller in date range
     */
    /*
    @Query(value = "SELECT COALESCE(SUM(od.quantity), 0) FROM order_detail od " +
                   "JOIN product p ON od.product_id = p.product_id " +
                   "JOIN orders o ON od.order_id = o.order_id " +
                   "WHERE p.seller_id = :sellerId " +
                   "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
                   "AND o.status IN ('DELIVERED', 'COMPLETED')", nativeQuery = true)
    Long getTotalProductsSoldBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Count unique products sold by seller in date range
     */
    /*
    @Query(value = "SELECT COUNT(DISTINCT od.product_id) FROM order_detail od " +
                   "JOIN product p ON od.product_id = p.product_id " +
                   "JOIN orders o ON od.order_id = o.order_id " +
                   "WHERE p.seller_id = :sellerId " +
                   "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
                   "AND o.status IN ('DELIVERED', 'COMPLETED')", nativeQuery = true)
    Long getUniqueProductsSoldBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Get top selling products by seller
     */
    /*
    @Query(value = "SELECT p.product_id, p.name, SUM(od.quantity) as total_sold, " +
                   "SUM(od.quantity * od.price_at_purchase) as total_revenue " +
                   "FROM order_detail od " +
                   "JOIN product p ON od.product_id = p.product_id " +
                   "JOIN orders o ON od.order_id = o.order_id " +
                   "WHERE p.seller_id = :sellerId " +
                   "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
                   "AND o.status IN ('DELIVERED', 'COMPLETED') " +
                   "GROUP BY p.product_id, p.name " +
                   "ORDER BY total_sold DESC", nativeQuery = true)
    List<Object[]> getTopSellingProductsBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Get product performance data with limit
     */
    /*
    @Query(value = "SELECT p.product_id, p.name, SUM(od.quantity) as total_sold, " +
                   "SUM(od.quantity * od.price_at_purchase) as total_revenue, " +
                   "COUNT(DISTINCT o.order_id) as order_count " +
                   "FROM order_detail od " +
                   "JOIN product p ON od.product_id = p.product_id " +
                   "JOIN orders o ON od.order_id = o.order_id " +
                   "WHERE p.seller_id = :sellerId " +
                   "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
                   "AND o.status IN ('DELIVERED', 'COMPLETED') " +
                   "GROUP BY p.product_id, p.name " +
                   "ORDER BY total_sold DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> getProductsPerformanceBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("limit") int limit); */
