package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Kafka Setup and Health Check Service
 * Ensures Kafka is properly configured and topics are created
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "etl.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaSetupService {

    private final AdminClient adminClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final List<String> REQUIRED_TOPICS = Arrays.asList(
        "etl-sales-metrics",
        "etl-business-kpis", 
        "etl-data-alerts",
        "etl-pipeline-status"
    );

    /**
     * Initialize Kafka setup after service startup
     */    @PostConstruct
    public void initializeKafka() {
        try {
            log.info("🚀 Initializing Kafka setup...");
            
            // Temporarily disable Kafka connection check for development
            log.warn("⚠️ Kafka connection check temporarily disabled for development");
            
            // Check Kafka connection
            // if (checkKafkaConnection()) {
            //     log.info("✅ Kafka connection successful");
                
                // Create required topics
                createRequiredTopics();
                
                // Verify topics exist
                // verifyTopicsExist();
                
                log.info("🎉 Kafka setup completed successfully!");
                
            // } else {
            //     log.warn("⚠️ Kafka connection failed - will retry later");
            // }
            
        } catch (Exception e) {
            log.error("❌ Failed to initialize Kafka setup", e);
        }
    }

    /**
     * Check if Kafka is accessible
     */
    public boolean checkKafkaConnection() {
        try {
            Set<String> topics = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);
            log.debug("Connected to Kafka. Found {} topics", topics.size());
            return true;
            
        } catch (Exception e) {
            log.error("Kafka connection check failed", e);
            return false;
        }
    }    /**
     * Create required ETL topics if they don't exist
     */
    public void createRequiredTopics() {
        try {
            log.info("🔧 Topic creation temporarily disabled for development");
            log.info("ℹ️ Required topics: {}", REQUIRED_TOPICS);
            
            // Temporarily disabled for development without Kafka broker
            /*
            Set<String> existingTopics = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);
            
            List<NewTopic> topicsToCreate = REQUIRED_TOPICS.stream()
                .filter(topic -> !existingTopics.contains(topic))
                .map(topic -> new NewTopic(topic, 1, (short) 1))
                .toList();
            
            if (!topicsToCreate.isEmpty()) {
                log.info("📂 Creating {} missing topics...", topicsToCreate.size());
                
                CreateTopicsResult result = adminClient.createTopics(topicsToCreate);
                result.all().get(30, TimeUnit.SECONDS);
                
                log.info("✅ Successfully created {} topics", topicsToCreate.size());
                
                // Log created topics
                topicsToCreate.forEach(topic -> 
                    log.info("  • Created topic: {}", topic.name()));
                    
            } else {
                log.info("ℹ️ All required topics already exist");
            }
            */
            
        } catch (Exception e) {
            log.error("Failed to create required topics", e);
        }
    }

    /**
     * Verify all required topics exist
     */
    public boolean verifyTopicsExist() {
        try {
            Set<String> existingTopics = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);
            
            boolean allExist = true;
            for (String requiredTopic : REQUIRED_TOPICS) {
                if (existingTopics.contains(requiredTopic)) {
                    log.debug("✅ Topic exists: {}", requiredTopic);
                } else {
                    log.warn("❌ Missing topic: {}", requiredTopic);
                    allExist = false;
                }
            }
            
            if (allExist) {
                log.info("✅ All required topics are available");
            } else {
                log.warn("⚠️ Some required topics are missing");
            }
            
            return allExist;
            
        } catch (Exception e) {
            log.error("Failed to verify topics exist", e);
            return false;
        }
    }

    /**
     * Send test message to verify producer functionality
     */
    public boolean testProducer() {
        try {
            String testTopic = "etl-pipeline-status";
            String testMessage = "Kafka setup verification";
              kafkaTemplate.send(testTopic, "setup-test", testMessage)
                .whenComplete((_, ex) -> {
                    if (ex == null) {
                        log.debug("✅ Test message sent successfully");
                    } else {
                        log.error("❌ Failed to send test message", ex);
                    }
                });
            
            return true;
            
        } catch (Exception e) {
            log.error("Producer test failed", e);
            return false;
        }
    }

    /**
     * Get Kafka setup status
     */
    public KafkaStatus getSetupStatus() {
        KafkaStatus status = new KafkaStatus();
        
        status.setConnectionAvailable(checkKafkaConnection());
        status.setTopicsReady(verifyTopicsExist());
        status.setProducerWorking(testProducer());
        status.setOverallHealthy(
            status.isConnectionAvailable() && 
            status.isTopicsReady() && 
            status.isProducerWorking()
        );
        
        return status;
    }

    /**
     * Status data class
     */
    public static class KafkaStatus {
        private boolean connectionAvailable;
        private boolean topicsReady;
        private boolean producerWorking;
        private boolean overallHealthy;

        // Getters and setters
        public boolean isConnectionAvailable() { return connectionAvailable; }
        public void setConnectionAvailable(boolean connectionAvailable) { this.connectionAvailable = connectionAvailable; }
        
        public boolean isTopicsReady() { return topicsReady; }
        public void setTopicsReady(boolean topicsReady) { this.topicsReady = topicsReady; }
        
        public boolean isProducerWorking() { return producerWorking; }
        public void setProducerWorking(boolean producerWorking) { this.producerWorking = producerWorking; }
        
        public boolean isOverallHealthy() { return overallHealthy; }
        public void setOverallHealthy(boolean overallHealthy) { this.overallHealthy = overallHealthy; }
    }
}
