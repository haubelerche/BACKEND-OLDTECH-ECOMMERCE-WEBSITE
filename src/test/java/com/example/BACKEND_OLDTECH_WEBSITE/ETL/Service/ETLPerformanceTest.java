package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for ETL pipeline
 * Tests execution time, memory usage, and throughput
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "etl.enabled=true",
    "etl.scheduler.enabled=false"
})
class ETLPerformanceTest {

    @Autowired
    private ETLOrchestrator etlOrchestrator;

    @Autowired
    private DataExtractorService dataExtractorService;

    @Test
    void testETLExecutionTime() {
        // Given: A test date
        LocalDate testDate = LocalDate.now().minusDays(1);
        StopWatch stopWatch = new StopWatch();

        // When: Run ETL and measure time
        stopWatch.start();
        ETLOrchestrator.ETLResult result = etlOrchestrator.runDailyETL(testDate);
        stopWatch.stop();

        // Then: Verify performance expectations
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        
        long executionTimeMs = stopWatch.getTotalTimeMillis();
        System.out.println("ETL execution time: " + executionTimeMs + "ms");
        
        // Performance assertion: ETL should complete within reasonable time
        assertTrue(executionTimeMs < 30000, "ETL should complete within 30 seconds"); // Adjust based on your requirements
    }

    @Test
    void testDataExtractionPerformance() {
        // Given: Multiple test dates
        List<LocalDate> testDates = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            testDates.add(LocalDate.now().minusDays(i));
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // When: Extract data for multiple dates
        List<ExtractedData> results = new ArrayList<>();
        for (LocalDate date : testDates) {
            ExtractedData data = dataExtractorService.extractDailyData(date);
            results.add(data);
        }

        stopWatch.stop();

        // Then: Verify performance
        assertEquals(7, results.size());
        
        long avgTimePerExtraction = stopWatch.getTotalTimeMillis() / testDates.size();
        System.out.println("Average extraction time per date: " + avgTimePerExtraction + "ms");
        
        // Performance assertion
        assertTrue(avgTimePerExtraction < 5000, "Data extraction should complete within 5 seconds per date");
    }

    @Test
    void testConcurrentETLExecution() throws Exception {
        // Given: Multiple dates to process concurrently
        List<LocalDate> testDates = List.of(
            LocalDate.now().minusDays(1),
            LocalDate.now().minusDays(2),
            LocalDate.now().minusDays(3)
        );

        ExecutorService executor = Executors.newFixedThreadPool(3);
        StopWatch stopWatch = new StopWatch();
        
        try {
            stopWatch.start();

            // When: Run ETL for multiple dates concurrently
            List<CompletableFuture<ETLOrchestrator.ETLResult>> futures = new ArrayList<>();
            
            for (LocalDate date : testDates) {
                CompletableFuture<ETLOrchestrator.ETLResult> future = CompletableFuture
                    .supplyAsync(() -> etlOrchestrator.runDailyETL(date), executor);
                futures.add(future);
            }

            // Wait for all to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            allFutures.get();

            stopWatch.stop();

            // Then: Verify all completed successfully
            for (CompletableFuture<ETLOrchestrator.ETLResult> future : futures) {
                ETLOrchestrator.ETLResult result = future.get();
                assertNotNull(result);
                assertEquals("SUCCESS", result.getStatus());
            }

            long totalTime = stopWatch.getTotalTimeMillis();
            System.out.println("Concurrent ETL execution time: " + totalTime + "ms");
            
            // Concurrent execution should be faster than sequential
            // (This is more of a baseline test - adjust expectations based on your system)
            assertTrue(totalTime < 60000, "Concurrent ETL should complete within 60 seconds");

        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testMemoryUsageMonitoring() {
        // Given: Get initial memory usage
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        LocalDate testDate = LocalDate.now().minusDays(1);

        // When: Run ETL and monitor memory
        ETLOrchestrator.ETLResult result = etlOrchestrator.runDailyETL(testDate);
        
        // Force garbage collection to get accurate reading
        System.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Then: Verify ETL completed and check memory usage
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        
        long memoryUsed = finalMemory - initialMemory;
        System.out.println("Memory used by ETL: " + (memoryUsed / 1024 / 1024) + " MB");
        
        // Memory usage assertion (adjust based on your requirements)
        assertTrue(memoryUsed < 100 * 1024 * 1024, "ETL should use less than 100MB of memory");
    }

    @Test
    void testETLThroughput() {
        // Given: Run ETL multiple times to test throughput
        int iterations = 3;
        List<Long> executionTimes = new ArrayList<>();
        LocalDate baseDate = LocalDate.now().minusDays(1);

        // When: Run ETL multiple times
        for (int i = 0; i < iterations; i++) {
            LocalDate testDate = baseDate.minusDays(i);
            
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            
            ETLOrchestrator.ETLResult result = etlOrchestrator.runDailyETL(testDate);
            
            stopWatch.stop();
            
            assertNotNull(result);
            assertEquals("SUCCESS", result.getStatus());
            
            executionTimes.add(stopWatch.getTotalTimeMillis());
        }

        // Then: Analyze throughput
        double averageTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
            
        System.out.println("Average ETL execution time over " + iterations + " iterations: " + averageTime + "ms");
        
        // Throughput assertion
        assertTrue(averageTime < 20000, "Average ETL execution should be under 20 seconds");
        
        // Check for performance degradation
        long firstRun = executionTimes.get(0);
        long lastRun = executionTimes.get(executionTimes.size() - 1);
        double degradation = ((double) lastRun - firstRun) / firstRun * 100;
        
        System.out.println("Performance degradation: " + degradation + "%");
        assertTrue(Math.abs(degradation) < 50, "Performance should not degrade more than 50%");
    }
}
