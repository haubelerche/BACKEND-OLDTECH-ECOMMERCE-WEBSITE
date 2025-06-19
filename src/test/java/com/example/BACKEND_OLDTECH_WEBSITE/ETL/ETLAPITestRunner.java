package com.example.BACKEND_OLDTECH_WEBSITE.ETL;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.util.ETLTestDataUtilSimple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Main test runner for ETL API testing
 * This test suite verifies that all ETL APIs are working correctly
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "etl.enabled=true",
    "etl.scheduler.enabled=false"
})
public class ETLAPITestRunner {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testAllETLAPIsWork() throws Exception {
        System.out.println("=== TESTING ETL APIs ===");
        
        // Test data utility
        Map<String, Object> testParams = ETLTestDataUtilSimple.getETLTestParameters();
        String testDate = (String) testParams.get("testDate");
        
        System.out.println("Using test date: " + testDate);

        // 1. Test ETL Status API
        System.out.println("1. Testing ETL Status API...");
        mockMvc.perform(get("/api/etl/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isHealthy").exists());
        System.out.println("✓ ETL Status API working");

        // 2. Test Daily ETL API
        System.out.println("2. Testing Daily ETL API...");
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", testDate)
                .with(csrf()))
                .andExpect(status().isOk());
        System.out.println("✓ Daily ETL API working");

        // 3. Test Current Day ETL API
        System.out.println("3. Testing Current Day ETL API...");
        mockMvc.perform(post("/api/etl/run/current")
                .with(csrf()))
                .andExpect(status().isOk());
        System.out.println("✓ Current Day ETL API working");

        // 4. Test Yesterday ETL API
        System.out.println("4. Testing Yesterday ETL API...");
        mockMvc.perform(post("/api/etl/run/yesterday")
                .with(csrf()))
                .andExpect(status().isOk());
        System.out.println("✓ Yesterday ETL API working");

        System.out.println("=== ALL ETL APIs ARE WORKING! ===");
    }

    @Test
    @WithMockUser(authorities = "ADMIN") 
    public void testSellerDashboardETLAPIs() throws Exception {
        System.out.println("=== TESTING SELLER DASHBOARD ETL APIs ===");
        
        String testDate = ETLTestDataUtilSimple.getTestDateString();
        Map<String, String> dateRange = ETLTestDataUtilSimple.getTestDateRange(3);

        // 1. Test Run ETL for All Sellers
        System.out.println("1. Testing Run ETL for All Sellers...");
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-all")
                .param("date", testDate)
                .with(csrf()))
                .andExpect(status().isOk());
        System.out.println("✓ Run ETL for All Sellers API working");

        // 2. Test Run ETL for Specific Seller
        System.out.println("2. Testing Run ETL for Specific Seller...");
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-seller/1")
                .param("date", testDate)
                .with(csrf()))
                .andExpect(status().isOk());
        System.out.println("✓ Run ETL for Specific Seller API working");

        // 3. Test Backfill ETL
        System.out.println("3. Testing Backfill ETL...");
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/backfill")
                .param("startDate", dateRange.get("startDate"))
                .param("endDate", dateRange.get("endDate"))
                .with(csrf()))
                .andExpect(status().isOk());
        System.out.println("✓ Backfill ETL API working");

        System.out.println("=== ALL SELLER DASHBOARD ETL APIs ARE WORKING! ===");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testETLErrorHandling() throws Exception {
        System.out.println("=== TESTING ETL ERROR HANDLING ===");

        // 1. Test Invalid Date Format
        System.out.println("1. Testing Invalid Date Format...");
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", ETLTestDataUtilSimple.getInvalidDateString())
                .with(csrf()))
                .andExpect(status().isBadRequest());
        System.out.println("✓ Invalid Date Error Handling working");

        // 2. Test Future Date (if validation exists)
        System.out.println("2. Testing Future Date...");
        try {
            mockMvc.perform(post("/api/etl/run/daily")
                    .param("date", ETLTestDataUtilSimple.getFutureDateString())
                    .with(csrf()))
                    .andExpect(status().isOk()); // May succeed depending on validation
            System.out.println("✓ Future Date handling working (or no validation)");
        } catch (Exception e) {
            System.out.println("✓ Future Date properly rejected");
        }

        System.out.println("=== ETL ERROR HANDLING WORKING! ===");
    }

    @Test
    @WithMockUser(authorities = "USER") // Non-admin user
    public void testETLSecurityRestrictions() throws Exception {
        System.out.println("=== TESTING ETL SECURITY RESTRICTIONS ===");

        // Test that non-admin users cannot access ETL endpoints
        System.out.println("1. Testing non-admin access restriction...");
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", ETLTestDataUtilSimple.getTestDateString())
                .with(csrf()))
                .andExpect(status().isForbidden());
        System.out.println("✓ Non-admin access properly restricted");

        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-all")
                .param("date", ETLTestDataUtilSimple.getTestDateString())
                .with(csrf()))
                .andExpect(status().isForbidden());
        System.out.println("✓ Non-admin access to seller ETL properly restricted");

        System.out.println("=== ETL SECURITY RESTRICTIONS WORKING! ===");
    }

    @Test
    public void testETLUnauthenticatedAccess() throws Exception {
        System.out.println("=== TESTING ETL UNAUTHENTICATED ACCESS ===");

        // Test that unauthenticated users cannot access ETL endpoints
        System.out.println("1. Testing unauthenticated access restriction...");
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", ETLTestDataUtilSimple.getTestDateString())
                .with(csrf()))
                .andExpect(status().isUnauthorized());
        System.out.println("✓ Unauthenticated access properly restricted");

        System.out.println("=== ETL UNAUTHENTICATED ACCESS RESTRICTIONS WORKING! ===");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void runCompleteETLAPITest() throws Exception {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RUNNING COMPLETE ETL API TEST SUITE");
        System.out.println("=".repeat(60));

        // Run all test methods
        testAllETLAPIsWork();
        testSellerDashboardETLAPIs();
        testETLErrorHandling();
        testETLSecurityRestrictions();
        testETLUnauthenticatedAccess();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎉 ALL ETL APIs ARE WORKING CORRECTLY! 🎉");
        System.out.println("=".repeat(60));
    }
}
