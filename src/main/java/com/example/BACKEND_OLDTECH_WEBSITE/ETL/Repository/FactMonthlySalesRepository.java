package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.FactMonthlySales;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactMonthlySalesRepository extends JpaRepository<FactMonthlySales, Long> {

    /**
     * Tìm dữ liệu fact theo year-month
     */
    @Query("SELECT fms FROM FactMonthlySales fms WHERE fms.yearMonth = :yearMonth")
    Optional<FactMonthlySales> findByYearMonth(@Param("yearMonth") String yearMonth);

    /**
     * Lấy dữ liệu trong khoảng thời gian
     */
    @Query("SELECT fms FROM FactMonthlySales fms " +
           "WHERE fms.yearMonth BETWEEN :startYearMonth AND :endYearMonth " +
           "ORDER BY fms.yearMonth ASC")
    List<FactMonthlySales> findByYearMonthRange(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);

    /**
     * Lấy 12 tháng gần nhất
     */
    @Query("SELECT fms FROM FactMonthlySales fms " +
           "ORDER BY fms.yearMonth DESC " +
           "LIMIT 12")
    List<FactMonthlySales> findLast12Months();

    /**
     * Tính tổng doanh thu trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(fms.totalRevenue), 0) FROM FactMonthlySales fms " +
           "WHERE fms.yearMonth BETWEEN :startYearMonth AND :endYearMonth")
    BigDecimal getTotalRevenueByRange(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);

    /**
     * Tính tổng đơn hàng trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(fms.totalOrders), 0) FROM FactMonthlySales fms " +
           "WHERE fms.yearMonth BETWEEN :startYearMonth AND :endYearMonth")
    Integer getTotalOrdersByRange(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);

    /**
     * Lấy số unique buyers trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(fms.uniqueBuyers), 0) FROM FactMonthlySales fms " +
           "WHERE fms.yearMonth BETWEEN :startYearMonth AND :endYearMonth")
    Integer getTotalBuyersByRange(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);

    /**
     * Lấy số unique sellers trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(fms.uniqueSellers), 0) FROM FactMonthlySales fms " +
           "WHERE fms.yearMonth BETWEEN :startYearMonth AND :endYearMonth")
    Integer getTotalSellersByRange(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);

    /**
     * Lấy dữ liệu bán sản phẩm theo category
     */
    @Query("SELECT fms.yearMonth, " +
           "fms.laptopsSold, fms.phonesSold, fms.pcsSold, " +
           "fms.headphonesSold, fms.speakersSold, fms.camerasSold, " +
           "fms.tabletsSold, fms.smartwatchesSold, fms.accessoriesSold " +
           "FROM FactMonthlySales fms " +
           "WHERE fms.yearMonth BETWEEN :startYearMonth AND :endYearMonth " +
           "ORDER BY fms.yearMonth ASC")
    List<Object[]> getProductCategorySales(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);

    /**
     * Lấy trend doanh thu và đơn hàng
     */
    @Query("SELECT fms.yearMonth, fms.totalRevenue, fms.totalOrders, fms.averageOrderValue " +
           "FROM FactMonthlySales fms " +
           "WHERE fms.yearMonth BETWEEN :startYearMonth AND :endYearMonth " +
           "ORDER BY fms.yearMonth ASC")
    List<Object[]> getRevenueTrend(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);

    /**
     * Lấy dữ liệu về satisfaction rates
     */
    @Query("SELECT fms.yearMonth, fms.sellerSatisfactionRate, fms.buyerSatisfactionRate " +
           "FROM FactMonthlySales fms " +
           "WHERE fms.yearMonth BETWEEN :startYearMonth AND :endYearMonth " +
           "ORDER BY fms.yearMonth ASC")
    List<Object[]> getSatisfactionTrend(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);    /**
     * Lấy conversion metrics
     */
    @Query("SELECT fms.yearMonth, fms.listingToSaleRate, " +
           "CASE WHEN fms.totalOrders > 0 THEN " +
           "((fms.totalOrders - COALESCE(fms.cancelledOrders, 0)) * 100.0 / fms.totalOrders) " +
           "ELSE 0 END as orderCompletionRate " +
           "FROM FactMonthlySales fms " +
           "WHERE fms.yearMonth BETWEEN :startYearMonth AND :endYearMonth " +
           "ORDER BY fms.yearMonth ASC")
    List<Object[]> getConversionTrend(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);/**
     * Tính growth rate so với tháng trước
     */
    @Query(value = "SELECT " +
           "fms1.year_month, " +
           "fms1.total_revenue, " +
           "fms2.total_revenue as previous_revenue, " +
           "CASE WHEN fms2.total_revenue > 0 THEN " +
           "((fms1.total_revenue - fms2.total_revenue) / fms2.total_revenue * 100) " +
           "ELSE 0 END as growth_rate " +
           "FROM fact_monthly_sales fms1 " +
           "LEFT JOIN fact_monthly_sales fms2 ON " +
           "DATE_ADD(STR_TO_DATE(CONCAT(fms2.year_month, '-01'), '%Y-%m-%d'), INTERVAL 1 MONTH) = " +
           "STR_TO_DATE(CONCAT(fms1.year_month, '-01'), '%Y-%m-%d') " +
           "WHERE fms1.year_month BETWEEN :startYearMonth AND :endYearMonth " +
           "ORDER BY fms1.year_month ASC", 
           nativeQuery = true)
    List<Object[]> getGrowthRates(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);

    /**
     * Kiểm tra dữ liệu tồn tại cho tháng cụ thể
     */
    @Query("SELECT COUNT(fms) > 0 FROM FactMonthlySales fms WHERE fms.yearMonth = :yearMonth")
    boolean existsByYearMonth(@Param("yearMonth") String yearMonth);

    /**
     * Lấy latest data quality score
     */
    @Query("SELECT AVG(fms.dataQualityScore) FROM FactMonthlySales fms " +
           "WHERE fms.yearMonth BETWEEN :startYearMonth AND :endYearMonth")
    Double getAverageDataQualityScore(
            @Param("startYearMonth") String startYearMonth,
            @Param("endYearMonth") String endYearMonth);
}
