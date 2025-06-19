package com.example.BACKEND_OLDTECH_WEBSITE.ETL;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple health check test to verify ETL components load correctly
 */
@SpringBootTest
@ActiveProfiles("test")
public class ETLHealthCheckTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testETLComponentsLoadCorrectly() {
        System.out.println("=== ETL HEALTH CHECK TEST ===");

        // Test 1: Check if ETL Orchestrator loads
        System.out.println("1. Checking ETL Orchestrator...");
        assertTrue(applicationContext.containsBean("ETLOrchestrator"), 
                   "ETL Orchestrator should be loaded");
        System.out.println("✓ ETL Orchestrator loaded successfully");

        // Test 2: Check if Data Extractor Service loads
        System.out.println("2. Checking Data Extractor Service...");
        assertTrue(applicationContext.containsBean("dataExtractorService"), 
                   "Data Extractor Service should be loaded");
        System.out.println("✓ Data Extractor Service loaded successfully");

        // Test 3: Check if Data Transformer Service loads
        System.out.println("3. Checking Data Transformer Service...");
        assertTrue(applicationContext.containsBean("dataTransformerService"), 
                   "Data Transformer Service should be loaded");
        System.out.println("✓ Data Transformer Service loaded successfully");

        // Test 4: Check if Data Loader Service loads
        System.out.println("4. Checking Data Loader Service...");
        assertTrue(applicationContext.containsBean("dataLoaderService"), 
                   "Data Loader Service should be loaded");
        System.out.println("✓ Data Loader Service loaded successfully");

        // Test 5: Check if ETL Controller loads (if enabled)
        System.out.println("5. Checking ETL Controller...");
        if (applicationContext.containsBean("ETLController")) {
            System.out.println("✓ ETL Controller loaded successfully");
        } else {
            System.out.println("ℹ ETL Controller not loaded (might be disabled)");
        }

        // Test 6: Check if Seller Dashboard ETL Service loads
        System.out.println("6. Checking Seller Dashboard ETL Service...");
        assertTrue(applicationContext.containsBean("sellerDashboardETLService"), 
                   "Seller Dashboard ETL Service should be loaded");
        System.out.println("✓ Seller Dashboard ETL Service loaded successfully");

        System.out.println("=== ALL ETL COMPONENTS LOADED SUCCESSFULLY! ===");
    }

    @Test
    public void testApplicationContextLoads() {
        System.out.println("=== APPLICATION CONTEXT HEALTH CHECK ===");
        
        assertNotNull(applicationContext, "Application context should not be null");
        System.out.println("✓ Application context loaded successfully");
        
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Total beans loaded: " + beanNames.length);
        
        // Count ETL-related beans
        long etlBeanCount = java.util.Arrays.stream(beanNames)
                .filter(name -> name.toLowerCase().contains("etl"))
                .count();
        
        System.out.println("ETL-related beans: " + etlBeanCount);
        assertTrue(etlBeanCount > 0, "Should have at least one ETL-related bean");
        
        System.out.println("=== APPLICATION CONTEXT HEALTHY! ===");
    }
}
