package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Kafka Health Check Service
 * Monitors Kafka cluster health và topic availability
 * Phục vụ cho academic demonstration về monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaHealthService {

    private final AdminClient adminClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Check Kafka cluster health
     */
    public boolean isKafkaHealthy() {
        try {
            // Test connection bằng cách list topics
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> topicNames = topics.names().get(5, TimeUnit.SECONDS);
            
            log.debug("Kafka cluster healthy. Available topics: {}", topicNames.size());
            return true;
            
        } catch (Exception e) {
            log.error("Kafka cluster health check failed", e);
            return false;
        }
    }

    /**
     * Check if ETL topics exist
     */
    public boolean areETLTopicsReady() {
        try {
            Set<String> topics = adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
            
            boolean hasAllTopics = topics.contains("etl-sales-metrics") &&
                                  topics.contains("etl-business-kpis") &&
                                  topics.contains("etl-data-alerts") &&
                                  topics.contains("etl-pipeline-status");
            
            log.debug("ETL topics ready: {}", hasAllTopics);
            return hasAllTopics;
            
        } catch (Exception e) {
            log.error("ETL topics check failed", e);
            return false;
        }
    }

    /**
     * Test Kafka producer bằng cách send test message
     */
    public boolean testKafkaProducer() {
        try {
            // Send test message
            kafkaTemplate.send("etl-pipeline-status", "health-check", 
                Map.of("test", true, "timestamp", System.currentTimeMillis()));
            
            log.debug("Kafka producer test successful");
            return true;
            
        } catch (Exception e) {
            log.error("Kafka producer test failed", e);
            return false;
        }
    }

    /**
     * Get comprehensive Kafka health status
     */
    public KafkaHealthStatus getHealthStatus() {
        KafkaHealthStatus status = new KafkaHealthStatus();
        
        status.setClusterHealthy(isKafkaHealthy());
        status.setTopicsReady(areETLTopicsReady());
        status.setProducerWorking(testKafkaProducer());
        status.setOverallHealth(status.isClusterHealthy() && 
                               status.isTopicsReady() && 
                               status.isProducerWorking());
        
        return status;
    }

    /**
     * Health status data class
     */
    public static class KafkaHealthStatus {
        private boolean clusterHealthy;
        private boolean topicsReady;
        private boolean producerWorking;
        private boolean overallHealth;

        // Getters and setters
        public boolean isClusterHealthy() { return clusterHealthy; }
        public void setClusterHealthy(boolean clusterHealthy) { this.clusterHealthy = clusterHealthy; }
        
        public boolean isTopicsReady() { return topicsReady; }
        public void setTopicsReady(boolean topicsReady) { this.topicsReady = topicsReady; }
        
        public boolean isProducerWorking() { return producerWorking; }
        public void setProducerWorking(boolean producerWorking) { this.producerWorking = producerWorking; }
        
        public boolean isOverallHealth() { return overallHealth; }
        public void setOverallHealth(boolean overallHealth) { this.overallHealth = overallHealth; }
    }
}
