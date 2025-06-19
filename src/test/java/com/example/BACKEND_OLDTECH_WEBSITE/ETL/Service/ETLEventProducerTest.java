package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ETL Event Producer - Kafka messaging
 */
@ExtendWith(MockitoExtension.class)
class ETLEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ETLEventProducer etlEventProducer;

    private CompletableFuture<SendResult<String, Object>> mockFuture;    @BeforeEach
    void setUp() {
        mockFuture = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        SendResult<String, Object> mockResult = mock(SendResult.class);
        mockFuture.complete(mockResult);
    }

    @Test
    void testPublishSalesMetricsEvent_Success() {
        // Given: Sales metrics data
        Map<String, Object> salesData = new HashMap<>();
        salesData.put("totalRevenue", 10000.0);
        salesData.put("totalOrders", 50);
        salesData.put("uniqueCustomers", 25);

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        // When: Publish sales metrics event
        assertDoesNotThrow(() -> {
            etlEventProducer.publishSalesMetricsEvent(salesData);
        });

        // Then: Verify Kafka template was called
        verify(kafkaTemplate).send(eq("etl-sales-metrics"), eq("sales-update"), any(Map.class));
    }

    @Test
    void testPublishBusinessKPIsEvent_Success() {
        // Given: Business KPIs data
        Map<String, Object> kpiData = new HashMap<>();
        kpiData.put("customerAcquisitionCost", 150.0);
        kpiData.put("customerLifetimeValue", 2500.0);
        kpiData.put("churnRate", 5.2);

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        // When: Publish business KPIs event
        assertDoesNotThrow(() -> {
            etlEventProducer.publishBusinessKPIsEvent(kpiData);
        });

        // Then: Verify Kafka template was called
        verify(kafkaTemplate).send(eq("etl-business-kpis"), eq("kpi-update"), any(Map.class));
    }

    @Test
    void testPublishDataQualityAlert_Success() {
        // Given: Data quality alert data
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("level", "WARNING");
        alertData.put("message", "Data volume below threshold");
        alertData.put("component", "DataExtractor");

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        // When: Publish data quality alert
        assertDoesNotThrow(() -> {
            etlEventProducer.publishDataQualityAlert(alertData);
        });

        // Then: Verify Kafka template was called
        verify(kafkaTemplate).send(eq("etl-data-alerts"), eq("quality-alert"), any(Map.class));
    }

    @Test
    void testPublishPipelineStatusEvent_Success() {
        // Given: Pipeline status data
        Map<String, Object> statusDetails = new HashMap<>();
        statusDetails.put("stage", "EXTRACTION");
        statusDetails.put("progress", 50);

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        // When: Publish pipeline status event
        assertDoesNotThrow(() -> {
            etlEventProducer.publishPipelineStatusEvent("RUNNING", "EXTRACTION", statusDetails);
        });

        // Then: Verify Kafka template was called
        verify(kafkaTemplate).send(eq("etl-pipeline-status"), eq("pipeline-status"), any(Map.class));
    }

    @Test
    void testPublishETLCompletionEvent_Success() {
        // Given: ETL completion data
        Map<String, Object> summary = new HashMap<>();
        summary.put("recordsProcessed", 1000);
        summary.put("dataQualityScore", 95.0);

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        // When: Publish ETL completion event
        assertDoesNotThrow(() -> {
            etlEventProducer.publishETLCompletionEvent("2024-01-15", "SUCCESS", 300L, summary);
        });

        // Then: Verify Kafka template was called
        verify(kafkaTemplate).send(eq("etl-pipeline-status"), eq("etl-completion"), any(Map.class));
    }

    @Test
    void testPublishTestEvent_Success() {
        // Given: Mock successful send
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        // When: Publish test event
        boolean result = etlEventProducer.publishTestEvent();

        // Then: Should return true for successful publication
        assertTrue(result);
        verify(kafkaTemplate).send(anyString(), anyString(), any(Map.class));
    }

    @Test
    void testPublishTestEvent_Failure() {
        // Given: Mock failed send
        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(failedFuture);

        // When: Publish test event
        boolean result = etlEventProducer.publishTestEvent();

        // Then: Should return false for failed publication
        assertFalse(result);
    }

    @Test
    void testGetProducerMetrics() {
        // When: Get producer metrics
        Map<String, Object> metrics = etlEventProducer.getProducerMetrics();

        // Then: Should return valid metrics
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("isHealthy"));
        assertTrue(metrics.containsKey("lastActivity"));
        assertTrue(metrics.containsKey("supportedTopics"));
        
        // Verify supported topics
        String[] supportedTopics = (String[]) metrics.get("supportedTopics");
        assertNotNull(supportedTopics);
        assertTrue(supportedTopics.length > 0);
        
        // Check for expected topics
        boolean hasExpectedTopics = java.util.Arrays.asList(supportedTopics).contains("etl-sales-metrics") &&
                                   java.util.Arrays.asList(supportedTopics).contains("etl-business-kpis") &&
                                   java.util.Arrays.asList(supportedTopics).contains("etl-data-alerts") &&
                                   java.util.Arrays.asList(supportedTopics).contains("etl-pipeline-status");
        assertTrue(hasExpectedTopics);
    }

    @Test
    void testEventStructure_SalesMetrics() {
        // Given: Sales metrics data
        Map<String, Object> salesData = new HashMap<>();
        salesData.put("totalRevenue", 10000.0);

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        // When: Publish event
        etlEventProducer.publishSalesMetricsEvent(salesData);

        // Then: Verify event structure
        verify(kafkaTemplate).send(eq("etl-sales-metrics"), eq("sales-update"), argThat(event -> {
            Map<String, Object> eventMap = (Map<String, Object>) event;
            return eventMap.containsKey("eventType") &&
                   eventMap.containsKey("data") &&
                   eventMap.containsKey("timestamp") &&
                   eventMap.containsKey("source") &&
                   eventMap.containsKey("version") &&
                   "SALES_METRICS_UPDATED".equals(eventMap.get("eventType")) &&
                   "ETL_PIPELINE".equals(eventMap.get("source"));
        }));
    }

    @Test
    void testEventStructure_DataQualityAlert() {
        // Given: Alert data
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("level", "ERROR");
        alertData.put("message", "Critical data quality issue");

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        // When: Publish event
        etlEventProducer.publishDataQualityAlert(alertData);

        // Then: Verify event structure
        verify(kafkaTemplate).send(eq("etl-data-alerts"), eq("quality-alert"), argThat(event -> {
            Map<String, Object> eventMap = (Map<String, Object>) event;
            return eventMap.containsKey("eventType") &&
                   eventMap.containsKey("alertLevel") &&
                   eventMap.containsKey("message") &&
                   eventMap.containsKey("details") &&
                   "DATA_QUALITY_ALERT".equals(eventMap.get("eventType")) &&
                   "ERROR".equals(eventMap.get("alertLevel"));
        }));
    }

    @Test
    void testKafkaTemplateException_HandledGracefully() {
        // Given: Kafka template throws exception
        when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("Kafka connection failed"));

        // When & Then: Should not propagate exception
        assertDoesNotThrow(() -> {
            etlEventProducer.publishSalesMetricsEvent(new HashMap<>());
        });

        assertDoesNotThrow(() -> {
            etlEventProducer.publishBusinessKPIsEvent(new HashMap<>());
        });

        assertDoesNotThrow(() -> {
            etlEventProducer.publishDataQualityAlert(new HashMap<>());
        });
    }
}
