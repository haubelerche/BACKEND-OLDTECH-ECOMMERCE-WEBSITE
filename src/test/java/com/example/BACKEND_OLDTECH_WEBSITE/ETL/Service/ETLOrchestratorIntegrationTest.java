package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.TransformedData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete ETL pipeline
 * Tests end-to-end ETL process with test database
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "etl.enabled=true",
    "etl.scheduler.enabled=false"
})
@Transactional
class ETLOrchestratorIntegrationTest {

    @Autowired
    private ETLOrchestrator etlOrchestrator;
    
    @Autowired
    private DataExtractorService dataExtractorService;
    
    @Autowired
    private DataTransformerService dataTransformerService;
    
    @Autowired
    private DataLoaderService dataLoaderService;

    @Test
    void testCompleteETLPipeline_Success() {
        // Given: A test date
        LocalDate testDate = LocalDate.now().minusDays(1);

        // When: Run complete ETL pipeline
        ETLOrchestrator.ETLResult result = etlOrchestrator.runDailyETL(testDate);

        // Then: Verify successful completion
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(testDate, result.getProcessDate());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());
        assertTrue(result.getDuration() >= 0);
        
        // Verify data quality score is reasonable
        assertTrue(result.getDataQualityScore() >= 0.0);
        assertTrue(result.getDataQualityScore() <= 100.0);
    }

    @Test
    void testETLPipeline_WithCurrentDay() {
        // When: Run ETL for current day
        ETLOrchestrator.ETLResult result = etlOrchestrator.runCurrentDayETL();

        // Then: Should complete successfully
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(LocalDate.now(), result.getProcessDate());
    }

    @Test
    void testETLPipeline_WithYesterday() {
        // When: Run ETL for yesterday
        ETLOrchestrator.ETLResult result = etlOrchestrator.runYesterdayETL();

        // Then: Should complete successfully
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(LocalDate.now().minusDays(1), result.getProcessDate());
    }

    @Test
    void testIndividualETLSteps() {
        // Given: A test date
        LocalDate testDate = LocalDate.now().minusDays(1);

        // Test 1: Extract phase
        ExtractedData extractedData = dataExtractorService.extractDailyData(testDate);
        assertNotNull(extractedData);
        assertEquals(testDate, extractedData.getExtractionDate());

        // Test 2: Transform phase
        TransformedData transformedData = dataTransformerService.transform(extractedData);
        assertNotNull(transformedData);
        assertEquals(testDate, transformedData.getProcessDate());
        assertNotNull(transformedData.getSalesMetrics());
        assertNotNull(transformedData.getBusinessKPIs());

        // Test 3: Load phase
        assertDoesNotThrow(() -> {
            dataLoaderService.loadToDataWarehouse(transformedData);
        });
    }

    @Test
    void testPipelineHealthCheck() {
        // When: Check pipeline health
        var status = etlOrchestrator.getCurrentPipelineStatus();

        // Then: Should return valid status information
        assertNotNull(status);
        assertTrue(status.containsKey("status"));
        assertTrue(status.containsKey("lastRun"));
        assertTrue(status.containsKey("isHealthy"));
    }
}
