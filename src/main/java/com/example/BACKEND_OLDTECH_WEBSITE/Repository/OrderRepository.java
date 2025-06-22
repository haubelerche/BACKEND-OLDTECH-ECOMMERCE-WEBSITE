package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Integer> {
    
    // ===============================
    // BASIC QUERIES
    // ===============================
    List<Orders> findByUserId(Integer userId);
    List<Orders> findByStatus(OrderStatusEnum status);
    List<Orders> findByUserIdAndStatus(Integer userId, OrderStatusEnum status);
    List<Orders> findByStatusAndDeliveredAtIsNotNullAndDeliveredAtBefore(
        OrderStatusEnum status, Timestamp beforeTime);
    List<Orders> findByStatusAndDeliveredAtBetween(
        OrderStatusEnum status, Timestamp startTime, Timestamp endTime);
    List<Orders> findByStatusAndDeliveredAtIsNotNull(OrderStatusEnum status);    /**
     * Tính tổng doanh thu của seller trong khoảng thời gian
     * Chỉ tính đơn hàng DELIVERED và COMPLETED
     */
    @Query(value = "SELECT COALESCE(SUM(o.total_amount), 0) FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('DELIVERED', 'COMPLETED')", 
           nativeQuery = true)
    BigDecimal getTotalRevenueBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Đếm tổng số đơn hàng của seller trong khoảng thời gian
     */
    @Query(value = "SELECT COUNT(DISTINCT o.order_id) FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate", 
           nativeQuery = true)
    Long getTotalOrdersBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Đếm số đơn hàng thành công của seller trong khoảng thời gian
     */
    @Query(value = "SELECT COUNT(DISTINCT o.order_id) FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('DELIVERED', 'COMPLETED')", 
           nativeQuery = true)
    Long getSuccessfulOrdersBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Đếm số đơn hàng bị hủy của seller trong khoảng thời gian
     */
    @Query(value = "SELECT COUNT(DISTINCT o.order_id) FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('CANCELLED', 'REJECTED')", 
           nativeQuery = true)
    Long getCancelledOrdersBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Đếm số đơn hàng đang pending của seller trong khoảng thời gian
     */
    @Query(value = "SELECT COUNT(DISTINCT o.order_id) FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('PENDING', 'PROCESSING')", 
           nativeQuery = true)
    Long getPendingOrdersBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Đếm số đơn hàng đang ship của seller trong khoảng thời gian
     */
    @Query(value = "SELECT COUNT(DISTINCT o.order_id) FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('SHIPPED', 'SHIPPING')", 
           nativeQuery = true)
    Long getShippedOrdersBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);    /**
     * Lấy dữ liệu doanh thu theo ngày cho biểu đồ
     */
    @Query(value = "SELECT DATE(o.order_time) as orderDate, COALESCE(SUM(o.total_amount), 0) as revenue " +
           "FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('DELIVERED', 'COMPLETED') " +
           "GROUP BY DATE(o.order_time) " +
           "ORDER BY DATE(o.order_time)", 
           nativeQuery = true)
    List<Object[]> getDailyRevenueBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu số đơn hàng theo ngày cho biểu đồ
     */
    @Query(value = "SELECT DATE(o.order_time) as orderDate, COUNT(DISTINCT o.order_id) as orderCount " +
           "FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(o.order_time) " +
           "ORDER BY DATE(o.order_time)", 
           nativeQuery = true)
    List<Object[]> getDailyOrdersBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu doanh thu theo tuần cho biểu đồ
     */
    @Query(value = "SELECT YEARWEEK(o.order_time) as yearWeek, COALESCE(SUM(o.total_amount), 0) as revenue " +
           "FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('DELIVERED', 'COMPLETED') " +
           "GROUP BY YEARWEEK(o.order_time) " +
           "ORDER BY YEARWEEK(o.order_time)", 
           nativeQuery = true)
    List<Object[]> getWeeklyRevenueBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu số đơn hàng theo tuần cho biểu đồ
     */
    @Query(value = "SELECT YEARWEEK(o.order_time) as yearWeek, COUNT(DISTINCT o.order_id) as orderCount " +
           "FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "GROUP BY YEARWEEK(o.order_time) " +
           "ORDER BY YEARWEEK(o.order_time)", 
           nativeQuery = true)
    List<Object[]> getWeeklyOrdersBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);    /**
     * Lấy dữ liệu doanh thu theo tháng cho biểu đồ
     */
    @Query(value = "SELECT YEAR(o.order_time) as year, MONTH(o.order_time) as month, " +
           "COALESCE(SUM(o.total_amount), 0) as revenue " +
           "FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('DELIVERED', 'COMPLETED') " +
           "GROUP BY YEAR(o.order_time), MONTH(o.order_time) " +
           "ORDER BY YEAR(o.order_time), MONTH(o.order_time)", 
           nativeQuery = true)
    List<Object[]> getMonthlyRevenueBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu số đơn hàng theo tháng cho biểu đồ
     */
    @Query(value = "SELECT YEAR(o.order_time) as year, MONTH(o.order_time) as month, " +
           "COUNT(DISTINCT o.order_id) as orderCount " +
           "FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(o.order_time), MONTH(o.order_time) " +
           "ORDER BY YEAR(o.order_time), MONTH(o.order_time)", 
           nativeQuery = true)
    List<Object[]> getMonthlyOrdersBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Đếm số khách hàng quay lại (có đơn hàng trước đó)
     */
    @Query(value = "SELECT COUNT(DISTINCT current_orders.user_id) FROM orders current_orders " +
           "JOIN order_detail cod ON current_orders.order_id = cod.order_id " +
           "JOIN product cp ON cod.product_id = cp.product_id " +
           "WHERE cp.seller_id = :sellerId " +
           "AND DATE(current_orders.order_time) BETWEEN :startDate AND :endDate " +
           "AND EXISTS (" +
           "  SELECT 1 FROM orders previous_orders " +
           "  JOIN order_detail pod ON previous_orders.order_id = pod.order_id " +
           "  JOIN product pp ON pod.product_id = pp.product_id " +
           "  WHERE pp.seller_id = :sellerId " +
           "  AND previous_orders.user_id = current_orders.user_id " +
           "  AND DATE(previous_orders.order_time) < :startDate" +
           ")", 
           nativeQuery = true)
    Long getReturningCustomerOrdersBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);    /**
     * Lấy dữ liệu để tính Average Order Value theo thời gian
     */
    @Query(value = "SELECT DATE(o.order_time) as orderDate, " +
           "COUNT(DISTINCT o.order_id) as orderCount, " +
           "COALESCE(SUM(o.total_amount), 0) as totalRevenue " +
           "FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('DELIVERED', 'COMPLETED') " +
           "GROUP BY DATE(o.order_time) " +
           "ORDER BY DATE(o.order_time)", 
           nativeQuery = true)
    List<Object[]> getDailyAOVDataBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu để tính AOV theo tuần
     */
    @Query(value = "SELECT YEARWEEK(o.order_time) as yearWeek, " +
           "COUNT(DISTINCT o.order_id) as orderCount, " +
           "COALESCE(SUM(o.total_amount), 0) as totalRevenue " +
           "FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('DELIVERED', 'COMPLETED') " +
           "GROUP BY YEARWEEK(o.order_time) " +
           "ORDER BY YEARWEEK(o.order_time)", 
           nativeQuery = true)
    List<Object[]> getWeeklyAOVDataBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu để tính AOV theo tháng
     */
    @Query(value = "SELECT YEAR(o.order_time) as year, MONTH(o.order_time) as month, " +
           "COUNT(DISTINCT o.order_id) as orderCount, " +
           "COALESCE(SUM(o.total_amount), 0) as totalRevenue " +
           "FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "AND o.status IN ('DELIVERED', 'COMPLETED') " +
           "GROUP BY YEAR(o.order_time), MONTH(o.order_time) " +
           "ORDER BY YEAR(o.order_time), MONTH(o.order_time)", 
           nativeQuery = true)
    List<Object[]> getMonthlyAOVDataBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Lấy thống kê theo status của đơn hàng
     */
    @Query(value = "SELECT o.status, COUNT(*) FROM orders o " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(o.order_time) BETWEEN :startDate AND :endDate " +
           "GROUP BY o.status", 
           nativeQuery = true)
    List<Object[]> getOrderStatusBreakdownBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}
