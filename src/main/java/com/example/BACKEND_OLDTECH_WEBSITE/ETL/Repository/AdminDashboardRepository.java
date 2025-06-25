package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminDashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // ===============================
    // SALES PERFORMANCE QUERIES
    // ===============================

    /**
     * Get GMV (Gross Merchandise Value) for a specific period type within date range
     */
    @Query("SELECT COALESCE(SUM(ad.grossMerchandiseValue), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false")
    BigDecimal getGmvByPeriodTypeAndDateRange(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get AOV (Average Order Value) for a specific period type within date range
     */
    @Query("SELECT COALESCE(AVG(ad.averageOrderValue), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false " +
           "AND ad.totalOrders > 0")
    BigDecimal getAovByPeriodTypeAndDateRange(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total platform revenue for a specific period type within date range
     */
    @Query("SELECT COALESCE(SUM(ad.platformRevenue), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false")
    BigDecimal getPlatformRevenueByPeriodTypeAndDateRange(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total orders count for a specific period type within date range
     */
    @Query("SELECT COALESCE(SUM(ad.totalOrders), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false")
    Integer getTotalOrdersByPeriodTypeAndDateRange(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total return orders count for a specific period type within date range
     */
    @Query("SELECT COALESCE(SUM(ad.totalReturnOrders), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false")
    Integer getTotalReturnOrdersByPeriodTypeAndDateRange(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get trend data for platform revenue and AOV over time
     * Returns [reportDate, platformRevenue, averageOrderValue]
     */
    @Query("SELECT ad.reportDate as date, ad.platformRevenue as revenue, ad.averageOrderValue as aov " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false " +
           "ORDER BY ad.reportDate ASC")
    List<Object[]> getSalesPerformanceTrendData(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ===============================
    // USER PERFORMANCE QUERIES
    // ===============================

    /**
     * Get total website visits for a specific period type within date range
     */
    @Query("SELECT COALESCE(SUM(ad.websiteVisits), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false")
    Integer getTotalWebsiteVisitsByPeriodType(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total new users for a specific period type within date range
     */
    @Query("SELECT COALESCE(SUM(ad.newUsers), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false")
    Integer getTotalNewUsersByPeriodType(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get average conversion rate for a specific period type within date range
     */
    @Query("SELECT COALESCE(AVG(ad.conversionRate), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false " +
           "AND ad.websiteVisits > 0")
    BigDecimal getAverageConversionRateByPeriodType(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get average purchase repeat rate (customer return rate) for a specific period type
     */
    @Query("SELECT COALESCE(AVG(ad.customerReturnRate), 0) FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false " +
           "AND ad.totalOrders > 0")
    BigDecimal getAveragePurchaseRepeatRateByPeriodType(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get user trend data (new users, website visits, total orders, and return orders)
     * Returns [reportDate, newUsers, websiteVisits, totalOrders, totalReturnOrders]
     */
    @Query("SELECT ad.reportDate as date, ad.newUsers as newUsers, ad.websiteVisits as visits, " +
           "ad.totalOrders as orders, ad.totalReturnOrders as returns " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false " +
           "ORDER BY ad.reportDate ASC")
    List<Object[]> getUserPerformanceTrendData(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get rate trend data (conversion rate and return rate)
     * Returns [reportDate, conversionRate, orderReturnRate]
     */
    @Query("SELECT ad.reportDate as date, ad.conversionRate as conversionRate, " +
           "ad.orderReturnRate as returnRate " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType AND ad.isForecast = false " +
           "ORDER BY ad.reportDate ASC")
    List<Object[]> getRatesTrendData(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ===============================
    // ARIMA FORECAST QUERIES
    // ===============================

    /**
     * Get historical monthly user data for ARIMA forecasting (past 9 months)
     * Returns [reportDate, newUsers]
     */
    @Query("SELECT ad.reportDate as date, ad.newUsers as users " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate >= :startDate AND ad.reportDate <= :endDate " +
           "AND ad.periodType = 'MONTHLY' AND ad.isForecast = false " +
           "ORDER BY ad.reportDate ASC")
    List<Object[]> getMonthlyUserDataForArima(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get historical monthly order data for ARIMA forecasting (past 9 months)
     * Returns [reportDate, totalOrders]
     */
    @Query("SELECT ad.reportDate as date, ad.totalOrders as orders " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate >= :startDate AND ad.reportDate <= :endDate " +
           "AND ad.periodType = 'MONTHLY' AND ad.isForecast = false " +
           "ORDER BY ad.reportDate ASC")
    List<Object[]> getMonthlyOrderDataForArima(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get historical monthly revenue data for ARIMA forecasting (past 9 months)
     * Returns [reportDate, platformRevenue]
     */
    @Query("SELECT ad.reportDate as date, ad.platformRevenue as revenue " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate >= :startDate AND ad.reportDate <= :endDate " +
           "AND ad.periodType = 'MONTHLY' AND ad.isForecast = false " +
           "ORDER BY ad.reportDate ASC")
    List<Object[]> getMonthlyRevenueDataForArima(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get forecast data for users, orders, and revenue (next 3 months)
     * Returns [reportDate, newUsers, totalOrders, platformRevenue]
     */
    @Query("SELECT ad.reportDate as date, ad.newUsers as users, ad.totalOrders as orders, " +
           "ad.platformRevenue as revenue " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate >= :startDate AND ad.reportDate <= :endDate " +
           "AND ad.periodType = 'MONTHLY' AND ad.isForecast = true " +
           "ORDER BY ad.reportDate ASC")
    List<Object[]> getArimaForecastData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get combined historical and forecast data for users
     * Returns [reportDate, newUsers, isForecast]
     */
    @Query("SELECT ad.reportDate as date, ad.newUsers as users, ad.isForecast as forecast " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate >= :startDate AND ad.reportDate <= :endDate " +
           "AND ad.periodType = 'MONTHLY' " +
           "ORDER BY ad.reportDate ASC")
    List<Object[]> getCombinedUserForecastData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get combined historical and forecast data for orders
     * Returns [reportDate, totalOrders, isForecast]
     */
    @Query("SELECT ad.reportDate as date, ad.totalOrders as orders, ad.isForecast as forecast " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate >= :startDate AND ad.reportDate <= :endDate " +
           "AND ad.periodType = 'MONTHLY' " +
           "ORDER BY ad.reportDate ASC")
    List<Object[]> getCombinedOrderForecastData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get combined historical and forecast data for revenue
     * Returns [reportDate, platformRevenue, isForecast]
     */
    @Query("SELECT ad.reportDate as date, ad.platformRevenue as revenue, ad.isForecast as forecast " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate >= :startDate AND ad.reportDate <= :endDate " +
           "AND ad.periodType = 'MONTHLY' " +
           "ORDER BY ad.reportDate ASC")
    List<Object[]> getCombinedRevenueForecastData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ===============================
    // SALES PERFORMANCE SUMMARY QUERY
    // ===============================

    /**
     * Get comprehensive sales performance metrics within a date range
     * Returns a single row with all key metrics
     */
    @Query("SELECT " +
           "COALESCE(SUM(ad.grossMerchandiseValue), 0) as gmv, " +
           "CASE WHEN COALESCE(SUM(ad.totalOrders), 0) > 0 " +
           "  THEN COALESCE(SUM(ad.grossMerchandiseValue), 0) / COALESCE(SUM(ad.totalOrders), 1) " +
           "  ELSE 0 END as aov, " +
           "COALESCE(SUM(ad.platformRevenue), 0) as platformRevenue, " +
           "COALESCE(SUM(ad.totalOrders), 0) as totalOrders, " +
           "COALESCE(SUM(ad.totalReturnOrders), 0) as totalReturnOrders, " +
           "COALESCE(SUM(ad.returnOrdersValue), 0) as returnOrdersValue, " +
           "COALESCE(SUM(ad.netGmv), 0) as netGmv " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType " +
           "AND ad.isForecast = false")
    Object[] getSalesPerformanceSummary(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ===============================
    // USER PERFORMANCE SUMMARY QUERY
    // ===============================

    /**
     * Get comprehensive user performance metrics within a date range
     * Returns a single row with all key metrics
     */
    @Query("SELECT " +
           "COALESCE(SUM(ad.websiteVisits), 0) as visits, " +
           "COALESCE(SUM(ad.uniqueVisitors), 0) as uniqueVisitors, " +
           "COALESCE(SUM(ad.newUsers), 0) as newUsers, " +
           "COALESCE(AVG(ad.conversionRate), 0) as conversionRate, " +
           "COALESCE(SUM(ad.returningCustomerOrders), 0) as returningOrders, " +
           "CASE WHEN COALESCE(SUM(ad.totalOrders), 0) > 0 " +
           "  THEN COALESCE(SUM(ad.returningCustomerOrders), 0) * 100.0 / COALESCE(SUM(ad.totalOrders), 1) " +
           "  ELSE 0 END as purchaseRepeatRate, " +
           "COALESCE(AVG(ad.orderReturnRate), 0) as returnRate " +
           "FROM AdminDashboard ad " +
           "WHERE ad.reportDate BETWEEN :startDate AND :endDate " +
           "AND ad.periodType = :periodType " +
           "AND ad.isForecast = false")
    Object[] getUserPerformanceSummary(
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
