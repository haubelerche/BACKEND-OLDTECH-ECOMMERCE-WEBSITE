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
     * Chạy ETL hàng ngày lúc 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void runDailyETL() {
        log.info("Starting scheduled daily ETL pipeline");
        
        try {
            // Xử lý dữ liệu hôm qua
            LocalDate processDate = LocalDate.now().minusDays(1);
            ETLOrchestrator.ETLResult result = etlOrchestrator.runDailyETL(processDate);
            
            log.info("Scheduled ETL completed successfully. Status: {}, Duration: {}s", 
                    result.getStatus(), result.getDuration());
                    
        } catch (Exception e) {
            log.error("Scheduled ETL failed", e);
        }
    }

    /**
     * Chạy ETL hàng giờ (xử lý dữ liệu ngày hiện tại)
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void runHourlyETL() {
        log.info("Starting scheduled hourly ETL pipeline");
        
        try {
            // Xử lý dữ liệu ngày hiện tại cho dashboard thời gian thực
            ETLOrchestrator.ETLResult result = etlOrchestrator.runCurrentDayETL();
            
            log.info("Scheduled hourly ETL completed. Status: {}, Duration: {}s", 
                    result.getStatus(), result.getDuration());
                    
        } catch (Exception e) {
            log.error("Scheduled hourly ETL failed", e);
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
