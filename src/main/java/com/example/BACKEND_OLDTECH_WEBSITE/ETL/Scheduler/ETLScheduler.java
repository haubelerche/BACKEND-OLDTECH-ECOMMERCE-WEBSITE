package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Scheduler;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.ETLOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Lịch chạy pipeline ETL
 * Tự động chạy pipeline ETL theo lịch
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "etl.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class ETLScheduler {

    private final ETLOrchestrator etlOrchestrator;

    /**
     * Chạy ETL hàng tuần vào 1:00 AM thứ 2 (Monday)
     */
    @Scheduled(cron = "0 0 1 * * MON")
    public void runWeeklyETL() {
        log.info("Starting scheduled weekly ETL pipeline");
        try {
            // Xử lý dữ liệu tuần trước
            LocalDate weekStartDate = LocalDate.now().minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
            ETLOrchestrator.ETLResult result = etlOrchestrator.runWeeklyETL(weekStartDate);
            log.info("Scheduled weekly ETL completed successfully. Status: {}, Duration: {}s", 
                    result.getStatus(), result.getDuration());
        } catch (Exception e) {
            log.error("Scheduled weekly ETL failed", e);
        }
    }

    /**
     * Kiểm tra tình trạng pipeline ETL - chạy mỗi 5 phút
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void healthCheck() {
        try {
            var status = etlOrchestrator.getCurrentPipelineStatus();
            log.debug("ETL Pipeline health check: {}", status.get("status"));
        } catch (Exception e) {
            log.error("ETL Pipeline health check failed", e);
        }
    }
}
