package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.ETLOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for ETL Controller REST endpoints
 */
@WebMvcTest(ETLController.class)
class ETLControllerTest {    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ETLOrchestrator etlOrchestrator;

    private ETLOrchestrator.ETLResult mockResult;

    @BeforeEach
    void setUp() {
        mockResult = new ETLOrchestrator.ETLResult();
        mockResult.setProcessDate(LocalDate.now().minusDays(1));
        mockResult.setStartTime(LocalDateTime.now().minusMinutes(5));
        mockResult.setEndTime(LocalDateTime.now());
        mockResult.setStatus("SUCCESS");
        mockResult.setDuration(300);
        mockResult.setRecordsExtracted(100);
        mockResult.setRecordsTransformed(100);
        mockResult.setRecordsLoaded(100);
        mockResult.setDataQualityScore(95.0);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testRunDailyETL_Success() throws Exception {
        // Given: Mock successful ETL execution
        when(etlOrchestrator.runDailyETL(any(LocalDate.class))).thenReturn(mockResult);

        // When & Then: Call API endpoint
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", "2024-01-15")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.processDate").value("2024-01-15"))
                .andExpect(jsonPath("$.duration").value(300))
                .andExpect(jsonPath("$.recordsExtracted").value(100))
                .andExpect(jsonPath("$.recordsTransformed").value(100))
                .andExpect(jsonPath("$.recordsLoaded").value(100))
                .andExpect(jsonPath("$.dataQualityScore").value(95.0));

        verify(etlOrchestrator).runDailyETL(LocalDate.of(2024, 1, 15));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testRunCurrentDayETL_Success() throws Exception {
        // Given: Mock successful current day ETL
        mockResult.setProcessDate(LocalDate.now());
        when(etlOrchestrator.runCurrentDayETL()).thenReturn(mockResult);

        // When & Then: Call API endpoint
        mockMvc.perform(post("/api/etl/run/current")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(etlOrchestrator).runCurrentDayETL();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testRunYesterdayETL_Success() throws Exception {
        // Given: Mock successful yesterday ETL
        when(etlOrchestrator.runYesterdayETL()).thenReturn(mockResult);

        // When & Then: Call API endpoint
        mockMvc.perform(post("/api/etl/run/yesterday")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(etlOrchestrator).runYesterdayETL();
    }    @Test
    @WithMockUser(authorities = "ADMIN")
    void testGetPipelineStatus_Success() throws Exception {
        // Given: Mock pipeline status
        java.util.Map<String, Object> statusMap = new java.util.HashMap<>();
        statusMap.put("status", "HEALTHY");
        statusMap.put("lastRun", LocalDateTime.now().minusHours(1).toString());
        statusMap.put("isHealthy", true);
        statusMap.put("nextScheduledRun", LocalDateTime.now().plusHours(23).toString());
        
        when(etlOrchestrator.getCurrentPipelineStatus()).thenReturn(statusMap);        // When & Then: Call API endpoint
        mockMvc.perform(get("/api/etl/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HEALTHY"))
                .andExpect(jsonPath("$.isHealthy").value(true));

        verify(etlOrchestrator).getCurrentPipelineStatus();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testRunDailyETL_Failure() throws Exception {
        // Given: Mock ETL failure
        mockResult.setStatus("FAILED");
        mockResult.setErrorMessage("Database connection failed");
        when(etlOrchestrator.runDailyETL(any(LocalDate.class))).thenReturn(mockResult);

        // When & Then: Call API endpoint
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", "2024-01-15")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorMessage").value("Database connection failed"));
    }

    @Test
    @WithMockUser(authorities = "USER") // Not admin
    void testRunDailyETL_Unauthorized() throws Exception {
        // When & Then: Call API endpoint without admin role
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", "2024-01-15")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(etlOrchestrator, never()).runDailyETL(any());
    }

    @Test
    void testRunDailyETL_Unauthenticated() throws Exception {
        // When & Then: Call API endpoint without authentication
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", "2024-01-15")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(etlOrchestrator, never()).runDailyETL(any());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testRunDailyETL_InvalidDate() throws Exception {
        // When & Then: Call API endpoint with invalid date format
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", "invalid-date")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(etlOrchestrator, never()).runDailyETL(any());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testRunDailyETL_Exception() throws Exception {
        // Given: Mock exception during ETL
        when(etlOrchestrator.runDailyETL(any(LocalDate.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then: Call API endpoint
        mockMvc.perform(post("/api/etl/run/daily")
                .param("date", "2024-01-15")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }
}
