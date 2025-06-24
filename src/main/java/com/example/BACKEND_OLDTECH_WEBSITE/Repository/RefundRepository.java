package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {
    

    List<Refund> findByStatus(RefundStatusEnum status);
    List<Refund> findByUserId(Integer userId);
    List<Refund> findByOrderId(Integer orderId); 
    @Query("SELECT DISTINCT r FROM Refund r " +
           "JOIN OrderDetail od ON r.orderId = od.order.orderId " +
           "JOIN Product p ON od.product.productId = p.productId " +
           "WHERE p.sellerId = :sellerId")
    List<Refund> findRefundsBySellerId(@Param("sellerId") Integer sellerId);}

    /*
     * Đếm số đơn đổi trả của seller trong khoảng thời gian
     */
     /*
    @Query(value = "SELECT COUNT(*) FROM refund r " +
           "JOIN orders o ON r.order_id = o.order_id " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(r.created_at) BETWEEN :startDate AND :endDate " +
           "AND r.status = 'APPROVED'", 
           nativeQuery = true)
    Long getReturnOrdersCountBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng giá trị đơn đổi trả của seller trong khoảng thời gian
     */
     /*
    @Query(value = "SELECT COALESCE(SUM(r.refund_amount), 0) FROM refund r " +
           "JOIN orders o ON r.order_id = o.order_id " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(r.created_at) BETWEEN :startDate AND :endDate " +
           "AND r.status = 'APPROVED'", 
           nativeQuery = true)
    BigDecimal getReturnOrdersValueBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu đổi trả theo ngày cho biểu đồ
     */
     /*
    @Query(value = "SELECT DATE(r.created_at) as refundDate, COUNT(*) as returnCount " +
           "FROM refund r " +
           "JOIN orders o ON r.order_id = o.order_id " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(r.created_at) BETWEEN :startDate AND :endDate " +
           "AND r.status = 'APPROVED' " +
           "GROUP BY DATE(r.created_at) " +
           "ORDER BY DATE(r.created_at)", 
           nativeQuery = true)
    List<Object[]> getDailyReturnsBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu đổi trả theo tuần cho biểu đồ
     */
     /*
    @Query(value = "SELECT YEARWEEK(r.created_at) as yearWeek, COUNT(*) as returnCount " +
           "FROM refund r " +
           "JOIN orders o ON r.order_id = o.order_id " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(r.created_at) BETWEEN :startDate AND :endDate " +
           "AND r.status = 'APPROVED' " +
           "GROUP BY YEARWEEK(r.created_at) " +
           "ORDER BY YEARWEEK(r.created_at)", 
           nativeQuery = true)
    List<Object[]> getWeeklyReturnsBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu đổi trả theo tháng cho biểu đồ
     */
     /*
    @Query(value = "SELECT YEAR(r.created_at) as year, MONTH(r.created_at) as month, " +
           "COUNT(*) as returnCount " +
           "FROM refund r " +
           "JOIN orders o ON r.order_id = o.order_id " +
           "JOIN order_detail od ON o.order_id = od.order_id " +
           "JOIN product p ON od.product_id = p.product_id " +
           "WHERE p.seller_id = :sellerId " +
           "AND DATE(r.created_at) BETWEEN :startDate AND :endDate " +
           "AND r.status = 'APPROVED' " +
           "GROUP BY YEAR(r.created_at), MONTH(r.created_at) " +
           "ORDER BY YEAR(r.created_at), MONTH(r.created_at)", 
           nativeQuery = true)
    List<Object[]> getMonthlyReturnsBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    /**
     * Tính tỷ lệ đổi trả (return rate) của seller
     */
     /*
    @Query(value = "SELECT " +
           "(SELECT COUNT(*) FROM refund r " +
           " JOIN orders o ON r.order_id = o.order_id " +
           " JOIN order_detail od ON o.order_id = od.order_id " +
           " JOIN product p ON od.product_id = p.product_id " +
           " WHERE p.seller_id = :sellerId " +
           " AND DATE(r.created_at) BETWEEN :startDate AND :endDate " +
           " AND r.status = 'APPROVED') * 1.0 / " +
           "NULLIF((SELECT COUNT(DISTINCT o2.order_id) FROM orders o2 " +
           " JOIN order_detail od2 ON o2.order_id = od2.order_id " +
           " JOIN product p2 ON od2.product_id = p2.product_id " +
           " WHERE p2.seller_id = :sellerId " +
           " AND DATE(o2.order_time) BETWEEN :startDate AND :endDate " +
           " AND o2.status IN ('DELIVERED', 'COMPLETED')), 0)", 
           nativeQuery = true)
    Double getReturnRateBySellerAndDateRange(
        @Param("sellerId") Integer sellerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}*/