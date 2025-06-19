package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * Repository đặc biệt cho các truy vấn phân tích phức tạp
 * Tách ra khỏi OrderRepository chính để tránh xung đột
 */
@Repository
public interface ETLOrderRepository extends JpaRepository<Orders, Integer> {    /**
     * Tìm đơn hàng trong khoảng thời gian cho ETL trích xuất
     */
    @Query("SELECT o FROM Orders o WHERE o.createdAt >= :startDate AND o.createdAt < :endDate")
    List<Orders> findOrdersByDateRange(@Param("startDate") Timestamp startDate, 
                                      @Param("endDate") Timestamp endDate);    /**
     * Lấy thống kê sản phẩm theo khoảng thời gian
     */
    @Query("SELECT COUNT(od.orderDetailId), SUM(od.priceAtPurchase) " +
           "FROM OrderDetail od " +
           "JOIN od.order o " +
           "WHERE od.product.productId = :productId " +
           "AND o.createdAt >= :startDate AND o.createdAt < :endDate")
    List<Object[]> getProductStatsByDateRange(@Param("productId") Integer productId,
                                             @Param("startDate") Timestamp startDate,
                                             @Param("endDate") Timestamp endDate);    /**
     * Lấy thống kê người bán theo khoảng thời gian
     */
    @Query("SELECT COUNT(DISTINCT o.orderId), SUM(od.priceAtPurchase) " +
           "FROM OrderDetail od " +
           "JOIN od.order o " +
           "JOIN od.product p " +
           "WHERE p.sellerId = :sellerId " +
           "AND o.createdAt >= :startDate AND o.createdAt < :endDate")
    List<Object[]> getSellerStatsByDateRange(@Param("sellerId") Integer sellerId,
                                            @Param("startDate") Timestamp startDate,
                                            @Param("endDate") Timestamp endDate);    /**
     * Lấy thống kê bán hàng theo ngày
     */
    @Query("SELECT DATE(o.createdAt), COUNT(o.orderId), SUM(o.totalAmount), COUNT(DISTINCT o.userId) " +
           "FROM Orders o " +
           "WHERE o.createdAt >= :startDate AND o.createdAt < :endDate " +
           "GROUP BY DATE(o.createdAt)")
    List<Object[]> getDailySalesMetrics(@Param("startDate") Timestamp startDate,
                                       @Param("endDate") Timestamp endDate);    /**
     * Lấy doanh thu theo ngành hàng cho khoảng thời gian
     */
    @Query("SELECT c.name, SUM(od.priceAtPurchase) " +
           "FROM OrderDetail od " +
           "JOIN od.product p " +
           "JOIN Category c ON c.id = p.categoryId " +
           "JOIN od.order o " +
           "WHERE o.createdAt >= :startDate AND o.createdAt < :endDate " +
           "GROUP BY c.name")
    List<Object[]> getRevenueByCategory(@Param("startDate") Timestamp startDate,
                                       @Param("endDate") Timestamp endDate);    /**
     * Lấy số lượng đơn hàng theo trạng thái cho khoảng thời gian
     */
    @Query("SELECT o.status, COUNT(o.orderId) " +
           "FROM Orders o " +
           "WHERE o.createdAt >= :startDate AND o.createdAt < :endDate " +
           "GROUP BY o.status")
    List<Object[]> getOrderCountByStatus(@Param("startDate") Timestamp startDate,
                                        @Param("endDate") Timestamp endDate);    /**
       * Lấy phân tích khách hàng theo nhóm
     */
    @Query("SELECT u.userId, COUNT(o.orderId), SUM(o.totalAmount), MAX(o.createdAt) " +
           "FROM Orders o " +
           "JOIN o.user u " +
           "WHERE o.createdAt >= :startDate " +
           "GROUP BY u.userId")
    List<Object[]> getCustomerSegmentData(@Param("startDate") Timestamp startDate);
}
