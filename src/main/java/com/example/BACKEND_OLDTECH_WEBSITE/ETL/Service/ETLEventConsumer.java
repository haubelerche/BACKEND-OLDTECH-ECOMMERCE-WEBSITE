package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Pure Kafka Consumer Service - Focus hoàn toàn vào Kafka cho bài cuối kỳ
 * Demonstrates: Event-driven architecture, message processing, consumer groups
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ETLEventConsumer {

    private final DashboardCacheService dashboardCacheService;

    /**
     * Consumer Group 1: Sales Metrics Processing
     * Demonstrates Kafka consumer patterns và data aggregation
     */
    @KafkaListener(topics = "etl-sales-metrics", groupId = "dashboard-consumer-group")
    public void handleSalesMetricsEvent(@Payload Map<String, Object> event,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       Acknowledgment acknowledgment) {
        try {
            log.info("Processing sales metrics event from topic: {}", topic);
            
            // Cache for API access (thay thế WebSocket)
            dashboardCacheService.cacheSalesMetrics(event);
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed sales metrics event");
            
        } catch (Exception e) {
            log.error("Error processing sales metrics event from topic: {}", topic, e);
        }
    }

    /**
     * Consumer Group 2: Business KPI Processing  
     */
    @KafkaListener(topics = "etl-business-kpis", groupId = "dashboard-consumer-group")
    public void handleBusinessKPIEvent(@Payload Map<String, Object> event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        try {
            log.info("Processing business KPI event from topic: {}", topic);
            dashboardCacheService.cacheBusinessKpis(event);
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing business KPI event from topic: {}", topic, e);
        }
    }

    /**
     * Consumer Group 3: Data Quality Alert Processing
     */
    @KafkaListener(topics = "etl-data-alerts", groupId = "dashboard-consumer-group")
    public void handleDataQualityAlerts(@Payload Map<String, Object> event,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       Acknowledgment acknowledgment) {
        try {
            log.info("Processing data quality alert from topic: {}", topic);
            dashboardCacheService.cacheDataQualityAlerts(event);
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing data quality alert from topic: {}", topic, e);
        }
    }

    /**
     * Consumer Group 4: Pipeline Status Processing
     */
    @KafkaListener(topics = "etl-pipeline-status", groupId = "dashboard-consumer-group")
    public void handlePipelineStatusEvent(@Payload Map<String, Object> event,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                         Acknowledgment acknowledgment) {
        try {
            log.info("Processing pipeline status event from topic: {}", topic);
            dashboardCacheService.cachePipelineStatus(event);
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing pipeline status event from topic: {}", topic, e);
        }
    }
}