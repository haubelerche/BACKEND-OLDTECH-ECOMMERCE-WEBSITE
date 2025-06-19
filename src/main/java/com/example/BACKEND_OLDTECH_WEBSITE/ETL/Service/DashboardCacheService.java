package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Cache Service for Dashboard Data
 * Thay thế WebSocket bằng caching mechanism cho Kafka-only architecture
 * Focus vào Kafka event processing và data aggregation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardCacheService {

    // In-memory cache cho dashboard data
    private final Map<String, Object> salesMetricsCache = new ConcurrentHashMap<>();
    private final Map<String, Object> businessKpiCache = new ConcurrentHashMap<>();
    private final Map<String, Object> dataQualityCache = new ConcurrentHashMap<>();
    private final Map<String, Object> pipelineStatusCache = new ConcurrentHashMap<>();

    /**
     * Cache sales metrics từ Kafka event
     */
    public void cacheSalesMetrics(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            Object data = event.get("data");
            
            salesMetricsCache.put("latest", data);
            salesMetricsCache.put("lastUpdated", LocalDateTime.now());
            salesMetricsCache.put("eventType", eventType);
            
            log.info("Cached sales metrics from Kafka event: {}", eventType);
            
        } catch (Exception e) {
            log.error("Error caching sales metrics", e);
        }
    }

    /**
     * Cache business KPIs từ Kafka event
     */
    public void cacheBusinessKpis(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            Object data = event.get("data");
            
            businessKpiCache.put("latest", data);
            businessKpiCache.put("lastUpdated", LocalDateTime.now());
            businessKpiCache.put("eventType", eventType);
            
            log.info("Cached business KPIs from Kafka event: {}", eventType);
            
        } catch (Exception e) {
            log.error("Error caching business KPIs", e);
        }
    }

    /**
     * Cache data quality alerts từ Kafka event
     */
    public void cacheDataQualityAlerts(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            Object alerts = event.get("alerts");
            
            dataQualityCache.put("latest", alerts);
            dataQualityCache.put("lastUpdated", LocalDateTime.now());
            dataQualityCache.put("eventType", eventType);
            
            log.info("Cached data quality alerts from Kafka event: {}", eventType);
            
        } catch (Exception e) {
            log.error("Error caching data quality alerts", e);
        }
    }

    /**
     * Cache pipeline status từ Kafka event
     */
    public void cachePipelineStatus(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            Object status = event.get("status");
            
            pipelineStatusCache.put("latest", status);
            pipelineStatusCache.put("lastUpdated", LocalDateTime.now());
            pipelineStatusCache.put("eventType", eventType);
            
            log.info("Cached pipeline status from Kafka event: {}", eventType);
            
        } catch (Exception e) {
            log.error("Error caching pipeline status", e);
        }
    }

    /**
     * Lấy tất cả cached data cho Dashboard API
     */
    public Map<String, Object> getAllCachedData() {
        return Map.of(
            "salesMetrics", salesMetricsCache,
            "businessKpis", businessKpiCache,
            "dataQuality", dataQualityCache,
            "pipelineStatus", pipelineStatusCache,
            "timestamp", LocalDateTime.now()
        );
    }

    /**
     * Lấy sales metrics cache
     */
    public Map<String, Object> getSalesMetricsCache() {
        return Map.copyOf(salesMetricsCache);
    }

    /**
     * Lấy business KPIs cache
     */
    public Map<String, Object> getBusinessKpiCache() {
        return Map.copyOf(businessKpiCache);
    }

    /**
     * Lấy data quality cache
     */
    public Map<String, Object> getDataQualityCache() {
        return Map.copyOf(dataQualityCache);
    }

    /**
     * Lấy pipeline status cache
     */
    public Map<String, Object> getPipelineStatusCache() {
        return Map.copyOf(pipelineStatusCache);
    }

    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        salesMetricsCache.clear();
        businessKpiCache.clear();
        dataQualityCache.clear();
        pipelineStatusCache.clear();
        log.info("Cleared all dashboard caches");
    }
}
