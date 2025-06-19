package com.example.BACKEND_OLDTECH_WEBSITE.ETL;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.ETLOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test for ETL system
 * Tests the complete ETL pipeline from API endpoints to database
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "etl.enabled=true",
    "etl.scheduler.enabled=false",
    "spring.kafka.producer.bootstrap-servers=localhost:9092",
    "spring.kafka.consumer.bootstrap-servers=localhost:9092"
})
@Transactional
public class ETLSystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ETLOrchestrator etlOrchestrator;

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testCompleteETLWorkflow() throws Exception {
        // Test 1: Check ETL status endpoint
        mockMvc.perform(get("/api/etl/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isHealthy").exists());

        // Test 2: Run ETL for yesterday
        String testDate = LocalDate.now().minusDays(1).toString();
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", testDate)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists());

        // Test 3: Run current day ETL
        mockMvc.perform(post("/api/etl/run/current")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists());

        // Test 4: Test seller dashboard ETL
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-all")
                .param("date", testDate)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testETLErrorHandling() throws Exception {
        // Test invalid date format
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", "invalid-date")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testETLSecurityRestrictions() throws Exception {
        // Test that non-admin users cannot access ETL endpoints
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", LocalDate.now().toString())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testETLUnauthenticatedAccess() throws Exception {
        // Test that unauthenticated users cannot access ETL endpoints
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", LocalDate.now().toString())
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testSellerDashboardETLEndpoints() throws Exception {
        String testDate = LocalDate.now().minusDays(1).toString();

        // Test run ETL for all sellers
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-all")
                .param("date", testDate)
                .with(csrf()))
                .andExpect(status().isOk());

        // Test run ETL for specific seller
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-seller/1")
                .param("date", testDate)
                .with(csrf()))
                .andExpect(status().isOk());

        // Test backfill ETL
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/backfill")
                .param("startDate", LocalDate.now().minusDays(3).toString())
                .param("endDate", LocalDate.now().minusDays(1).toString())
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    public void testETLOrchestratorDirectly() {
        // Test ETL orchestrator service directly
        LocalDate testDate = LocalDate.now().minusDays(1);
        
        // This should complete without throwing exceptions
        ETLOrchestrator.ETLResult result = etlOrchestrator.runDailyETL(testDate);
        
        // Basic assertions
        assert result != null;
        assert result.getProcessDate().equals(testDate);
        assert result.getStatus() != null;
        assert result.getDuration() >= 0;
    }
}
