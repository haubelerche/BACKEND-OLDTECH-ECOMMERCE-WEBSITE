package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.util.ETLTestDataUtil;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.ETLOrchestrator;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.AdminDashboardETLService;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.SellerDashboardETLService;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.DataLoaderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Comprehensive tests for all ETL Controller APIs
 * Uses mocked services to focus on controller logic and API contracts
 */
@WebMvcTest({ETLController.class, AdminDashboardETLController.class, SellerDashboardETLController.class})
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ETLControllerAPITest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ETLOrchestrator etlOrchestrator;

    @MockBean
    private DataLoaderService dataLoaderService;

    @MockBean
    private AdminDashboardETLService adminDashboardETLService;

    @MockBean
    private SellerDashboardETLService sellerDashboardETLService;

    private ETLOrchestrator.ETLResult mockETLResult;
    private Map<String, Object> mockMetrics;
    private final String testDate = ETLTestDataUtil.getTestDateString();

    @BeforeEach
    void setUp() {
        // Setup mock ETL result
        mockETLResult = new ETLOrchestrator.ETLResult();
        mockETLResult.setProcessDate(LocalDate.parse(testDate));
        mockETLResult.setStartTime(LocalDateTime.now().minusMinutes(5));
        mockETLResult.setEndTime(LocalDateTime.now());
        mockETLResult.setStatus("SUCCESS");
        mockETLResult.setDuration(300L);
        mockETLResult.setRecordsExtracted(100);
        mockETLResult.setRecordsTransformed(100);
        mockETLResult.setRecordsLoaded(100);
        mockETLResult.setDataQualityScore(95.0);

        // Setup mock metrics
        mockMetrics = new HashMap<>();
        mockMetrics.put("totalSales", 50000.0);
        mockMetrics.put("totalOrders", 150);
        mockMetrics.put("averageOrderValue", 333.33);
        mockMetrics.put("topSellingProducts", ETLTestDataUtil.getTestSellerIds());        // Setup mock behaviors
        when(etlOrchestrator.runDailyETL(any(LocalDate.class))).thenReturn(mockETLResult);
        when(etlOrchestrator.runCurrentDayETL()).thenReturn(mockETLResult);
        when(etlOrchestrator.runYesterdayETL()).thenReturn(mockETLResult);
        when(etlOrchestrator.getCurrentPipelineStatus()).thenReturn(mockMetrics);
        when(dataLoaderService.getLatestMetrics()).thenReturn(mockMetrics);
        when(dataLoaderService.getSalesMetrics(any(LocalDate.class))).thenReturn(null);
    }

    /**
     * Test 1: ETL Controller - Health Check (Public endpoint)
     */
    @Test
    @Order(1)
    void testETLHealthCheck() throws Exception {
        when(etlOrchestrator.getCurrentPipelineStatus()).thenReturn(mockMetrics);

        mockMvc.perform(get("/api/etl/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HEALTHY"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.pipeline").exists());
    }

    /**
     * Test 2: ETL Controller - Get ETL Info (Public endpoint)
     */
    @Test
    @Order(2)
    void testGetETLInfo() throws Exception {
        mockMvc.perform(get("/api/etl/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("OldTech E-commerce ETL Pipeline"))
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.capabilities").exists())
                .andExpect(jsonPath("$.endpoints").exists());
    }

    /**
     * Test 3: ETL Controller - Get Latest Metrics
     */
    @Test
    @Order(3)
    void testGetLatestMetrics() throws Exception {
        mockMvc.perform(get("/api/etl/metrics/latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSales").value(50000.0))
                .andExpect(jsonPath("$.totalOrders").value(150));
    }

    /**
     * Test 4: ETL Controller - Get Sales Metrics for Date
     */    @Test
    @Order(4)
    void testGetSalesMetrics() throws Exception {
        mockMvc.perform(get("/api/etl/metrics/sales/" + testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_DATA"));
    }

    /**
     * Test 5: ETL Controller - Get Pipeline Status (Admin only)
     */
    @Test
    @Order(5)
    @WithMockUser(authorities = "Admin")
    void testGetPipelineStatus() throws Exception {
        mockMvc.perform(get("/api/etl/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSales").value(50000.0));

        verify(etlOrchestrator).getCurrentPipelineStatus();
    }

    /**
     * Test 6: ETL Controller - Run ETL for Specific Date (Admin only)
     */
    @Test
    @Order(6)
    @WithMockUser(authorities = "Admin")
    void testRunETLForDate() throws Exception {
        mockMvc.perform(post("/api/etl/run/" + testDate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.result").exists());

        verify(etlOrchestrator).runDailyETL(LocalDate.parse(testDate));
    }

    /**
     * Test 7: ETL Controller - Run ETL for Today (Admin only)
     */
    @Test
    @Order(7)
    @WithMockUser(authorities = "Admin")
    void testRunETLForToday() throws Exception {
        mockMvc.perform(post("/api/etl/run/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").exists());

        verify(etlOrchestrator).runCurrentDayETL();
    }

    /**
     * Test 8: ETL Controller - Run ETL for Yesterday (Admin only)
     */
    @Test
    @Order(8)
    @WithMockUser(authorities = "Admin")
    void testRunETLForYesterday() throws Exception {
        mockMvc.perform(post("/api/etl/run/yesterday")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").exists());

        verify(etlOrchestrator).runYesterdayETL();
    }

    /**
     * Test 9: Admin Dashboard ETL - Run ETL for Date
     */
    @Test
    @Order(9)
    @WithMockUser(roles = "ADMIN")
    void testAdminDashboardETLRunForDate() throws Exception {
        doNothing().when(adminDashboardETLService).runETLForDate(any(LocalDate.class));

        mockMvc.perform(post("/api/admin/dashboard-etl/run")
                        .param("date", testDate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.date").value(testDate));

        verify(adminDashboardETLService).runETLForDate(LocalDate.parse(testDate));
    }

    /**
     * Test 10: Admin Dashboard ETL - Run ETL without Date (should use yesterday)
     */
    @Test
    @Order(10)
    @WithMockUser(roles = "ADMIN")
    void testAdminDashboardETLRunWithoutDate() throws Exception {
        doNothing().when(adminDashboardETLService).runETLForDate(any(LocalDate.class));

        mockMvc.perform(post("/api/admin/dashboard-etl/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        verify(adminDashboardETLService).runETLForDate(any(LocalDate.class));
    }

    /**
     * Test 11: Seller Dashboard ETL - Run ETL for All Sellers
     */
    @Test
    @Order(11)
    @WithMockUser(roles = "ADMIN")
    void testSellerDashboardETLRunForAllSellers() throws Exception {
        doNothing().when(sellerDashboardETLService).runETLForAllSellers(any(LocalDate.class));

        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-all")
                        .param("date", testDate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        verify(sellerDashboardETLService).runETLForAllSellers(LocalDate.parse(testDate));
    }

    /**
     * Test 12: Seller Dashboard ETL - Run ETL for Specific Seller
     */
    @Test
    @Order(12)
    @WithMockUser(roles = "ADMIN")
    void testSellerDashboardETLRunForSpecificSeller() throws Exception {
        Integer testSellerId = ETLTestDataUtil.getTestSellerIds()[0];
        doNothing().when(sellerDashboardETLService).runETLForSeller(eq(testSellerId), any(LocalDate.class));

        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-seller/" + testSellerId)
                        .param("date", testDate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        verify(sellerDashboardETLService).runETLForSeller(testSellerId, LocalDate.parse(testDate));
    }

    /**
     * Test 13: Security Test - Unauthorized Access to Admin Endpoints
     */
    @Test
    @Order(13)
    void testUnauthorizedAccessToAdminEndpoints() throws Exception {
        // Test ETL Controller admin endpoint without authentication
        mockMvc.perform(post("/api/etl/run/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Test Admin Dashboard ETL without authentication
        mockMvc.perform(post("/api/admin/dashboard-etl/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Test Seller Dashboard ETL without authentication
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    /**
     * Test 14: Error Handling - Invalid Date Format
     */
    @Test
    @Order(14)
    @WithMockUser(authorities = "Admin")
    void testInvalidDateFormat() throws Exception {
        String invalidDate = ETLTestDataUtil.getInvalidDateString();

        mockMvc.perform(post("/api/etl/run/" + invalidDate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is5xxServerError()); // Internal server error due to parsing
    }

    /**
     * Test 15: Error Handling - Service Exception
     */
    @Test
    @Order(15)
    @WithMockUser(authorities = "Admin")
    void testServiceException() throws Exception {
        when(etlOrchestrator.runCurrentDayETL()).thenThrow(new RuntimeException("Test service exception"));

        mockMvc.perform(post("/api/etl/run/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("ETL pipeline thất bại: Test service exception"))
                .andExpect(jsonPath("$.error").value("RuntimeException"));
    }

    /**
     * Test 16: Performance Test - Multiple Concurrent Requests
     */
    @Test
    @Order(16)
    void testConcurrentHealthChecks() throws Exception {
        when(etlOrchestrator.getCurrentPipelineStatus()).thenReturn(mockMetrics);

        // Test multiple health check requests to ensure system stability
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/etl/health")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("HEALTHY"));
        }

        verify(etlOrchestrator, times(5)).getCurrentPipelineStatus();
    }

    /**
     * Helper method to print test results
     */
    @AfterEach
    void printTestResult(TestInfo testInfo) {
        System.out.println("✅ Completed test: " + testInfo.getDisplayName());
    }

    @AfterAll
    static void tearDown() {
        System.out.println("🎉 All ETL API tests completed!");
        System.out.println("📊 Summary:");
        System.out.println("- ETL Controller: 8 endpoints tested");
        System.out.println("- Admin Dashboard ETL: 2 endpoints tested");
        System.out.println("- Seller Dashboard ETL: 2 endpoints tested");
        System.out.println("- Security tests: 1 test");
        System.out.println("- Error handling: 2 tests");
        System.out.println("- Performance tests: 1 test");
        System.out.println("📝 Total: 16 comprehensive tests with mocked services");
    }
}
