package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.KafkaHealthService;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.KafkaSetupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Kafka Management Controller
 * Quản lý và monitor Kafka cluster cho ETL pipeline
 * Demonstration cho academic purposes
 */
@RestController
@RequestMapping("/kafka-management")
@RequiredArgsConstructor
@Slf4j
public class KafkaManagementController {

    private final KafkaHealthService kafkaHealthService;
    private final KafkaSetupService kafkaSetupService;

    /**
     * Check Kafka cluster health
     */
    @GetMapping("/health")
    public ResponseEntity<?> checkKafkaHealth() {
        try {
            KafkaHealthService.KafkaHealthStatus status = kafkaHealthService.getHealthStatus();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Kafka health check completed",
                "health", status,
                "architecture", "KAFKA_ONLY_ETL"
            ));
            
        } catch (Exception e) {
            log.error("Kafka health check failed", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Kafka health check failed",
                "details", e.getMessage()
            ));
        }
    }

    /**
     * Test Kafka producer/consumer functionality
     */
    @PostMapping("/test")

    public ResponseEntity<?> testKafkaFunctionality() {
        try {
            boolean producerWorking = kafkaHealthService.testKafkaProducer();
            boolean topicsReady = kafkaHealthService.areETLTopicsReady();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Kafka functionality test completed",
                "producerWorking", producerWorking,
                "topicsReady", topicsReady,
                "overallStatus", producerWorking && topicsReady ? "HEALTHY" : "ISSUES_DETECTED"
            ));
            
        } catch (Exception e) {
            log.error("Kafka functionality test failed", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Kafka test failed",
                "details", e.getMessage()
            ));
        }
    }

    /**
     * Get Kafka cluster information
     */
    @GetMapping("/info")

    public ResponseEntity<?> getKafkaInfo() {
        return ResponseEntity.ok(Map.of(
            "architecture", "PURE_KAFKA_ETL",
            "removedComponents", Map.of(
                "redis", "Removed - not needed for low traffic tech marketplace",
                "websocket", "Removed - replaced with REST API polling"
            ),
            "kafkaTopics", Map.of(
                "etl-sales-metrics", "Sales data processing",
                "etl-business-kpis", "Business intelligence metrics",
                "etl-data-alerts", "Data quality monitoring",
                "etl-pipeline-status", "ETL pipeline monitoring"
            ),
            "consumerGroups", Map.of(
                "dashboard-consumer-group", "Processes all ETL events for dashboard cache"
            ),
            "academicValue", Map.of(
                "eventDriven", "Demonstrates event-driven architecture",
                "messaging", "Shows Kafka producer/consumer patterns",
                "scalability", "Designed for horizontal scaling",
                "monitoring", "Health checks and status monitoring"
            )
        ));
    }

    /**
     * Initialize Kafka setup (create topics, etc.)
     */
    @PostMapping("/setup")

    public ResponseEntity<?> setupKafka() {
        try {
            kafkaSetupService.initializeKafka();
            KafkaSetupService.KafkaStatus status = kafkaSetupService.getSetupStatus();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Kafka setup completed",
                "status", status,
                "setupTime", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Kafka setup failed", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Kafka setup failed",
                "details", e.getMessage()
            ));
        }
    }
}
