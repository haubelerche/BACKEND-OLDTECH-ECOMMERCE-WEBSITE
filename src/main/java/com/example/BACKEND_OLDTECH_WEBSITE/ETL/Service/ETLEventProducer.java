package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ETL Event Producer Service
 * Responsible for publishing ETL events to Kafka topics
 * Demonstrates Kafka producer patterns for event-driven architecture
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "etl.enabled", havingValue = "true", matchIfMissing = false)
public class ETLEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish sales metrics event to Kafka
     */
    public void publishSalesMetricsEvent(Map<String, Object> salesData) {
        try {
            Map<String, Object> event = createSalesMetricsEvent(salesData);
            
            // Send to sales metrics topic
            sendEventAsync("etl-sales-metrics", "sales-update", event, 
                "Sales metrics event published successfully", 
                "Failed to publish sales metrics event");
                
        } catch (Exception e) {
            log.error("Error creating sales metrics event", e);
        }
    }

    /**
     * Publish business KPIs event to Kafka
     */
    public void publishBusinessKPIsEvent(Map<String, Object> kpiData) {
        try {
            Map<String, Object> event = createBusinessKPIsEvent(kpiData);
            
            // Send to business KPIs topic
            sendEventAsync("etl-business-kpis", "kpi-update", event,
                "Business KPIs event published successfully",
                "Failed to publish business KPIs event");
                
        } catch (Exception e) {
            log.error("Error creating business KPIs event", e);
        }
    }

    /**
     * Publish data quality alert to Kafka
     */
    public void publishDataQualityAlert(Map<String, Object> alertData) {
        try {
            Map<String, Object> event = createDataQualityAlertEvent(alertData);
            
            // Send to data alerts topic
            sendEventAsync("etl-data-alerts", "quality-alert", event,
                "Data quality alert published successfully",
                "Failed to publish data quality alert");
                
        } catch (Exception e) {
            log.error("Error creating data quality alert event", e);
        }
    }

    /**
     * Publish pipeline status event to Kafka
     */
    public void publishPipelineStatusEvent(String status, String stage, Map<String, Object> details) {
        try {
            Map<String, Object> event = createPipelineStatusEvent(status, stage, details);
            
            // Send to pipeline status topic
            sendEventAsync("etl-pipeline-status", "pipeline-status", event,
                "Pipeline status event published successfully",
                "Failed to publish pipeline status event");
                
        } catch (Exception e) {
            log.error("Error creating pipeline status event", e);
        }
    }

    /**
     * Publish ETL completion event with summary
     */
    public void publishETLCompletionEvent(String processDate, String status, long duration, Map<String, Object> summary) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "ETL_COMPLETION");
            event.put("processDate", processDate);
            event.put("status", status);
            event.put("duration", duration);
            event.put("summary", summary);
            event.put("timestamp", System.currentTimeMillis());
            event.put("completedAt", LocalDateTime.now().toString());
            
            // Send to pipeline status topic
            sendEventAsync("etl-pipeline-status", "etl-completion", event,
                "ETL completion event published successfully",
                "Failed to publish ETL completion event");
                
        } catch (Exception e) {
            log.error("Error creating ETL completion event", e);
        }
    }    // ===============================
    // PRIVATE HELPER METHODS
    // ===============================

    /**
     * Send event asynchronously to Kafka topic
     */
    private void sendEventAsync(String topic, String key, Map<String, Object> event, 
                               String successMessage, String errorMessage) {        try {
            kafkaTemplate.send(topic, key, event)
                .whenComplete((_, ex) -> {
                    if (ex == null) {
                        log.debug("{} - Topic: {}, Key: {}", successMessage, topic, key);
                    } else {
                        log.error("{} - Topic: {}, Key: {}", errorMessage, topic, key, ex);
                    }
                });
            
        } catch (Exception e) {
            log.error("Error sending event to topic: {} with key: {}", topic, key, e);
        }
    }

    /**
     * Create sales metrics event structure
     */
    private Map<String, Object> createSalesMetricsEvent(Map<String, Object> salesData) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "SALES_METRICS_UPDATED");
        event.put("data", salesData);
        event.put("timestamp", System.currentTimeMillis());
        event.put("source", "ETL_PIPELINE");
        event.put("version", "1.0");
        return event;
    }

    /**
     * Create business KPIs event structure
     */
    private Map<String, Object> createBusinessKPIsEvent(Map<String, Object> kpiData) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "BUSINESS_KPIS_UPDATED");
        event.put("data", kpiData);
        event.put("timestamp", System.currentTimeMillis());
        event.put("source", "ETL_PIPELINE");
        event.put("version", "1.0");
        return event;
    }

    /**
     * Create data quality alert event structure
     */
    private Map<String, Object> createDataQualityAlertEvent(Map<String, Object> alertData) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "DATA_QUALITY_ALERT");
        event.put("alertLevel", alertData.getOrDefault("level", "INFO"));
        event.put("message", alertData.getOrDefault("message", "Data quality check"));
        event.put("details", alertData);
        event.put("timestamp", System.currentTimeMillis());
        event.put("source", "ETL_PIPELINE");
        event.put("version", "1.0");
        return event;
    }

    /**
     * Create pipeline status event structure
     */
    private Map<String, Object> createPipelineStatusEvent(String status, String stage, Map<String, Object> details) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PIPELINE_STATUS_UPDATE");
        event.put("status", status);
        event.put("stage", stage);
        event.put("details", details != null ? details : new HashMap<>());
        event.put("timestamp", System.currentTimeMillis());
        event.put("source", "ETL_PIPELINE");
        event.put("version", "1.0");
        return event;
    }

    /**
     * Publish test event for health checks
     */
    public boolean publishTestEvent() {
        try {
            Map<String, Object> testEvent = new HashMap<>();
            testEvent.put("eventType", "HEALTH_CHECK");
            testEvent.put("message", "Test event from ETL Producer");
            testEvent.put("timestamp", System.currentTimeMillis());
            testEvent.put("source", "ETL_HEALTH_CHECK");
            
            kafkaTemplate.send("etl-pipeline-status", "health-check", testEvent);
            log.debug("Test event published successfully");
            return true;
            
        } catch (Exception e) {
            log.error("Failed to publish test event", e);
            return false;
        }
    }

    /**
     * Get producer metrics for monitoring
     */
    public Map<String, Object> getProducerMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get basic producer info
            metrics.put("isHealthy", true);
            metrics.put("lastActivity", LocalDateTime.now().toString());
            metrics.put("supportedTopics", new String[]{
                "etl-sales-metrics", 
                "etl-business-kpis", 
                "etl-data-alerts", 
                "etl-pipeline-status"
            });
            
        } catch (Exception e) {
            metrics.put("isHealthy", false);
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }
}
