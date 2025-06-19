package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminKPISchedulerService {

    private final AdminDashboardService adminDashboardService;

    /**
     * Auto refresh KPIs every day at 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void refreshDailyKPIs() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            adminDashboardService.refreshKPIs(yesterday);
            log.info("Daily KPIs refreshed successfully for date: {}", yesterday);
        } catch (Exception e) {
            log.error("Error refreshing daily KPIs", e);
        }
    }

    /**
     * Auto refresh KPIs every hour during business hours (8 AM - 8 PM)
     */
    @Scheduled(cron = "0 0 8-20 * * ?")
    public void refreshCurrentDayKPIs() {
        try {
            LocalDate today = LocalDate.now();
            adminDashboardService.refreshKPIs(today);
            log.info("Current day KPIs refreshed successfully for date: {}", today);
        } catch (Exception e) {
            log.error("Error refreshing current day KPIs", e);
        }
    }
}
