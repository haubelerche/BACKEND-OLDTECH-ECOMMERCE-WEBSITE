package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.SellerDashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SellerDashboardRepository extends JpaRepository<SellerDashboard, Integer> {

    /**
     * Tìm dashboard data theo seller và khoảng thời gian
     */
    @Query("SELECT sd FROM SellerDashboard sd WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate BETWEEN :startDate AND :endDate " +
           "AND sd.isForecast = false " +
           "ORDER BY sd.reportDate DESC")
    List<SellerDashboard> findBySellerIdAndDateRange(
            @Param("sellerId") Integer sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tìm dashboard data cho dự đoán
     */
    @Query("SELECT sd FROM SellerDashboard sd WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate BETWEEN :startDate AND :endDate " +
           "AND sd.isForecast = true " +
           "ORDER BY sd.reportDate ASC")
    List<SellerDashboard> findForecastBySellerIdAndDateRange(
            @Param("sellerId") Integer sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tìm data theo seller, ngày và loại period
     */
    @Query("SELECT sd FROM SellerDashboard sd WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate = :reportDate AND sd.periodType = :periodType")
    Optional<SellerDashboard> findBySellerIdAndReportDateAndPeriodType(
            @Param("sellerId") Integer sellerId,
            @Param("reportDate") LocalDate reportDate,
            @Param("periodType") String periodType);

    /**
     * Lấy dữ liệu tổng hợp theo seller và period type
     */
    @Query("SELECT sd FROM SellerDashboard sd WHERE sd.sellerId = :sellerId " +
           "AND sd.periodType = :periodType " +
           "AND sd.isForecast = false " +
           "ORDER BY sd.reportDate DESC")
    List<SellerDashboard> findBySellerIdAndPeriodType(
            @Param("sellerId") Integer sellerId,
            @Param("periodType") String periodType);

    /**
     * Tính tổng doanh thu theo seller trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(sd.totalRevenue), 0) FROM SellerDashboard sd " +
           "WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate BETWEEN :startDate AND :endDate " +
           "AND sd.isForecast = false")
    BigDecimal getTotalRevenueBySellerAndDateRange(
            @Param("sellerId") Integer sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng số đơn hàng theo seller trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(sd.totalOrders), 0) FROM SellerDashboard sd " +
           "WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate BETWEEN :startDate AND :endDate " +
           "AND sd.isForecast = false")
    Integer getTotalOrdersBySellerAndDateRange(
            @Param("sellerId") Integer sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng lượt truy cập theo seller trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(sd.totalVisits), 0) FROM SellerDashboard sd " +
           "WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate BETWEEN :startDate AND :endDate " +
           "AND sd.isForecast = false")
    Integer getTotalVisitsBySellerAndDateRange(
            @Param("sellerId") Integer sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Lấy data của tháng trước để so sánh
     */
    @Query("SELECT sd FROM SellerDashboard sd WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate BETWEEN :startDate AND :endDate " +
           "AND sd.periodType = 'MONTHLY' " +
           "AND sd.isForecast = false " +
           "ORDER BY sd.reportDate DESC")
    List<SellerDashboard> getPreviousMonthData(
            @Param("sellerId") Integer sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu 9 tháng gần nhất cho ARIMA
     */
    @Query("SELECT sd FROM SellerDashboard sd WHERE sd.sellerId = :sellerId " +
           "AND sd.periodType = 'MONTHLY' " +
           "AND sd.isForecast = false " +
           "AND sd.reportDate >= :startDate " +
           "ORDER BY sd.reportDate ASC")
    List<SellerDashboard> getMonthlyDataForArima(
            @Param("sellerId") Integer sellerId,
            @Param("startDate") LocalDate startDate);

    /**
     * Xóa dữ liệu dự đoán cũ
     */
    @Query("DELETE FROM SellerDashboard sd WHERE sd.sellerId = :sellerId " +
           "AND sd.isForecast = true " +
           "AND sd.reportDate >= :fromDate")
    void deleteForecastData(
            @Param("sellerId") Integer sellerId,
            @Param("fromDate") LocalDate fromDate);

    /**
     * Kiểm tra xem có dữ liệu trong ngày không
     */
    @Query("SELECT COUNT(sd) > 0 FROM SellerDashboard sd " +
           "WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate = :reportDate " +
           "AND sd.periodType = :periodType")
    boolean existsBySellerIdAndReportDateAndPeriodType(
            @Param("sellerId") Integer sellerId,
            @Param("reportDate") LocalDate reportDate,
            @Param("periodType") String periodType);

    /**
     * Lấy latest dashboard record theo seller
     */
    @Query("SELECT sd FROM SellerDashboard sd WHERE sd.sellerId = :sellerId " +
           "AND sd.isForecast = false " +
           "ORDER BY sd.reportDate DESC, sd.createdAt DESC")
    List<SellerDashboard> findLatestBySellerIdOrderByReportDateDesc(
            @Param("sellerId") Integer sellerId);

    /**
     * Tính average conversion rate trong khoảng thời gian
     */
    @Query("SELECT AVG(sd.conversionRate) FROM SellerDashboard sd " +
           "WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate BETWEEN :startDate AND :endDate " +
           "AND sd.isForecast = false " +
           "AND sd.conversionRate IS NOT NULL")
    BigDecimal getAverageConversionRate(
            @Param("sellerId") Integer sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tính average return rate trong khoảng thời gian
     */
    @Query("SELECT AVG(sd.returnRate) FROM SellerDashboard sd " +
           "WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate BETWEEN :startDate AND :endDate " +
           "AND sd.isForecast = false " +
           "AND sd.returnRate IS NOT NULL")
    BigDecimal getAverageReturnRate(
            @Param("sellerId") Integer sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu dashboard cho biểu đồ trend
     */
    @Query("SELECT sd FROM SellerDashboard sd WHERE sd.sellerId = :sellerId " +
           "AND sd.reportDate BETWEEN :startDate AND :endDate " +
           "AND sd.periodType = :periodType " +
           "AND sd.isForecast = false " +
           "ORDER BY sd.reportDate ASC")
    List<SellerDashboard> getTrendData(
            @Param("sellerId") Integer sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("periodType") String periodType);
}
