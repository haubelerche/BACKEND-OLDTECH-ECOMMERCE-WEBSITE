package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.FactMonthlySales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho bảng Fact trong Data Warehouse
 */
@Repository
public interface DataWarehouseRepository extends JpaRepository<FactMonthlySales, Long> {    /**
     * Tìm dữ liệu bán hàng theo tháng
     */
    Optional<FactMonthlySales> findByMonthKey(LocalDate monthKey);

    /**
     * Tìm dữ liệu bán hàng theo ngày (alias cho tháng)
     */
    default Optional<FactMonthlySales> findByDateKey(LocalDate date) {
        return findByMonthKey(date);
    }    /**
     * Lấy dữ liệu bán hàng cho khoảng thời gian
     */
    @Query("SELECT f FROM FactMonthlySales f WHERE f.monthKey >= :startDate AND f.monthKey <= :endDate ORDER BY f.monthKey")
    List<FactMonthlySales> findByDateRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    /**
     * Lấy xu hướng bán hàng gần đây
     */
    @Query("SELECT f FROM FactMonthlySales f ORDER BY f.monthKey DESC LIMIT :limit")
    List<FactMonthlySales> findRecentSalesData(@Param("limit") int limit);

    /**
     * Tìm record theo ngày xử lý (thay thế Redis cache)
     */
    @Query("SELECT f FROM FactMonthlySales f WHERE f.monthKey = :processDate")
    FactMonthlySales findByProcessDate(@Param("processDate") LocalDate processDate);
    
    /**
     * Lấy record mới nhất (thay thế Redis latest cache)
     */
    @Query("SELECT f FROM FactMonthlySales f ORDER BY f.monthKey DESC LIMIT 1")
    FactMonthlySales findTopByOrderByProcessDateDesc();
}
