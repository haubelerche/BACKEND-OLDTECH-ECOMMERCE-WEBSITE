package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminDashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminDashboardRepository extends JpaRepository<AdminDashboard, Integer> {

    /**
     * Tìm admin dashboard data theo khoảng thời gian
     */
    @Query("SELECT ad FROM AdminDashboard ad WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.isForecast = false ORDER BY ad.reportDate DESC")
    List<AdminDashboard> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tìm admin dashboard data cho dự đoán
     */
    @Query("SELECT ad FROM AdminDashboard ad WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.isForecast = true ORDER BY ad.reportDate ASC")
    List<AdminDashboard> findForecastByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tìm data theo ngày và loại period
     */
    @Query("SELECT ad FROM AdminDashboard ad WHERE ad.reportDate = :reportDate " +
           "AND ad.periodType = :periodType")
    Optional<AdminDashboard> findByReportDateAndPeriodType(
            @Param("reportDate") LocalDate reportDate,
            @Param("periodType") String periodType);

    /**
     * Lấy dữ liệu tổng hợp theo period type
     */
    @Query("SELECT ad FROM AdminDashboard ad WHERE ad.periodType = :periodType " +
           "AND ad.isForecast = false ORDER BY ad.reportDate DESC")
    List<AdminDashboard> findByPeriodType(@Param("periodType") String periodType);

    /**
     * Tính tổng GMV theo khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(ad.grossMerchandiseValue), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.isForecast = false")
    BigDecimal getTotalGmvByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng số đơn hàng theo khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(ad.totalOrders), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.isForecast = false")
    Integer getTotalOrdersByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng doanh thu nền tảng theo khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(ad.platformRevenue), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.isForecast = false")
    BigDecimal getTotalPlatformRevenueByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng lượt truy cập theo khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(ad.websiteVisits), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.isForecast = false")
    Integer getTotalWebsiteVisitsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Lấy data của tháng trước để so sánh
     */
    @Query("SELECT ad FROM AdminDashboard ad WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = 'MONTHLY' AND ad.isForecast = false " +
           "ORDER BY ad.reportDate DESC")
    List<AdminDashboard> getPreviousMonthData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu 9 tháng gần nhất cho ARIMA
     */
    @Query("SELECT ad FROM AdminDashboard ad WHERE ad.periodType = 'MONTHLY' " +
           "AND ad.isForecast = false AND ad.reportDate >= :startDate " +
           "ORDER BY ad.reportDate ASC")
    List<AdminDashboard> getMonthlyDataForArima(@Param("startDate") LocalDate startDate);

    /**
     * Xóa dữ liệu dự đoán cũ
     */
    @Query("DELETE FROM AdminDashboard ad WHERE ad.isForecast = true " +
           "AND ad.reportDate >= :fromDate")
    void deleteForecastData(@Param("fromDate") LocalDate fromDate);

    /**
     * Kiểm tra xem có dữ liệu trong ngày không
     */
    @Query("SELECT COUNT(ad) > 0 FROM AdminDashboard ad " +
           "WHERE ad.reportDate = :reportDate AND ad.periodType = :periodType")
    boolean existsByReportDateAndPeriodType(
            @Param("reportDate") LocalDate reportDate,
            @Param("periodType") String periodType);

    /**
     * Lấy latest dashboard record
     */
    @Query("SELECT ad FROM AdminDashboard ad WHERE ad.isForecast = false " +
           "ORDER BY ad.reportDate DESC, ad.createdAt DESC")
    List<AdminDashboard> findLatestOrderByReportDateDesc();

    /**
     * Tính average conversion rate trong khoảng thời gian
     */
    @Query("SELECT AVG(ad.conversionRate) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.isForecast = false AND ad.conversionRate IS NOT NULL")
    BigDecimal getAverageConversionRate(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Tính average return rate trong khoảng thời gian
     */
    @Query("SELECT AVG(ad.orderReturnRate) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.isForecast = false AND ad.orderReturnRate IS NOT NULL")
    BigDecimal getAverageOrderReturnRate(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Lấy dữ liệu dashboard cho biểu đồ trend
     */
    @Query("SELECT ad FROM AdminDashboard ad WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false " +
           "ORDER BY ad.reportDate ASC")
    List<AdminDashboard> getTrendData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("periodType") String periodType);

    /**
     * Lấy tổng cảnh báo trong khoảng thời gian
     */
    @Query("SELECT " +
           "COALESCE(SUM(ad.pendingSellers), 0) as totalPendingSellers, " +
           "COALESCE(SUM(ad.newComplaints), 0) as totalNewComplaints, " +
           "COALESCE(SUM(ad.fraudTransactions), 0) as totalFraudTransactions, " +
           "COALESCE(SUM(ad.systemAlerts), 0) as totalSystemAlerts, " +
           "COALESCE(SUM(ad.pendingProducts), 0) as totalPendingProducts " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.isForecast = false")
    Object[] getTotalAlertsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Lấy top performance metrics
     */
    @Query("SELECT ad FROM AdminDashboard ad WHERE ad.isForecast = false " +
           "AND ad.reportDate >= :startDate ORDER BY ad.grossMerchandiseValue DESC")
    List<AdminDashboard> getTopPerformanceDays(@Param("startDate") LocalDate startDate);

    /**
     * Lấy dữ liệu cho growth comparison
     */
    @Query("SELECT ad FROM AdminDashboard ad WHERE ad.reportDate IN :dates " +
           "AND ad.periodType = :periodType AND ad.isForecast = false " +
           "ORDER BY ad.reportDate")
    List<AdminDashboard> getGrowthComparisonData(
            @Param("dates") List<LocalDate> dates,
            @Param("periodType") String periodType);

    /**
     * Lấy summary metrics cho quick stats
     */
    @Query("SELECT " +
           "COALESCE(SUM(ad.grossMerchandiseValue), 0) as totalGmv, " +
           "COALESCE(SUM(ad.totalOrders), 0) as totalOrders, " +
           "COALESCE(SUM(ad.websiteVisits), 0) as totalVisits, " +
           "COALESCE(SUM(ad.newUsers), 0) as totalNewUsers, " +
           "COALESCE(AVG(ad.conversionRate), 0) as avgConversionRate " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate = :date AND ad.isForecast = false")
    Object[] getQuickStatsByDate(@Param("date") LocalDate date);

    /**
     * Lấy data quality score trung bình
     */
    @Query("SELECT AVG(ad.dataQualityScore) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate")
    BigDecimal getAverageDataQualityScore(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
