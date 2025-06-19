package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.AdminKPIMetrics;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminKPIMetricsRepository extends JpaRepository<AdminKPIMetrics, Long> {

    Optional<AdminKPIMetrics> findByMetricDateAndMetricType(LocalDate metricDate, AdminKPIMetrics.MetricType metricType);

    List<AdminKPIMetrics> findByMetricTypeAndMetricDateBetweenOrderByMetricDateDesc(
            AdminKPIMetrics.MetricType metricType, 
            LocalDate startDate, 
            LocalDate endDate);

    @Query("SELECT k FROM AdminKPIMetrics k WHERE k.metricType = :metricType ORDER BY k.metricDate DESC")
    List<AdminKPIMetrics> findLatestByMetricType(@Param("metricType") AdminKPIMetrics.MetricType metricType);

    @Query("SELECT k FROM AdminKPIMetrics k WHERE k.metricDate >= :startDate AND k.metricType = :metricType ORDER BY k.metricDate ASC")
    List<AdminKPIMetrics> findByDateRangeAndType(@Param("startDate") LocalDate startDate, @Param("metricType") AdminKPIMetrics.MetricType metricType);

    void deleteByMetricDateBefore(LocalDate date);
}
