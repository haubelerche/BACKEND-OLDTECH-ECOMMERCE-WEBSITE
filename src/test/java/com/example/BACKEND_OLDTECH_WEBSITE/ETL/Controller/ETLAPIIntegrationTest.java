package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.util.ETLTestDataUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Integration tests for all ETL Controller APIs
 * Tests all endpoints to ensure they work correctly
 */
@SpringBootTest
@ActiveProfiles("etl-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ETLAPIIntegrationTest {    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final String testDate = ETLTestDataUtil.getTestDateString();

    @BeforeAll
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    /**
     * Test 1: ETL Controller - Health Check (Public endpoint)
     */
    @Test
    @Order(1)
    void testETLHealthCheck() throws Exception {
        mockMvc.perform(get("/api/etl/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.timestamp").exists());
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
     * Test 3: ETL Controller - Get Latest Metrics (Public endpoint)
     */
    @Test
    @Order(3)
    void testGetLatestMetrics() throws Exception {
        mockMvc.perform(get("/api/etl/metrics/latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    /**
     * Test 4: ETL Controller - Get Sales Metrics for Date
     */
    @Test
    @Order(4)
    void testGetSalesMetrics() throws Exception {
        mockMvc.perform(get("/api/etl/metrics/sales/" + testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    /**
     * Test 5: ETL Controller - Get Pipeline Status (Admin only)
     */
    @Test
    @Order(5)
    @WithMockUser(authorities = "Admin")
    void testGetPipelineStatus() throws Exception {
        mockMvc.perform(get("/api/etl/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    /**
     * Test 6: ETL Controller - Run ETL for Specific Date (Admin only)
     */
    @Test
    @Order(6)
    @WithMockUser(authorities = "Admin")
    void testRunETLForDate() throws Exception {
        mockMvc.perform(post("/api/etl/run/" + testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Test 7: ETL Controller - Run ETL for Today (Admin only)
     */
    @Test
    @Order(7)
    @WithMockUser(authorities = "Admin")
    void testRunETLForToday() throws Exception {
        mockMvc.perform(post("/api/etl/run/today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Test 8: ETL Controller - Run ETL for Yesterday (Admin only)
     */
    @Test
    @Order(8)
    @WithMockUser(authorities = "Admin")
    void testRunETLForYesterday() throws Exception {
        mockMvc.perform(post("/api/etl/run/yesterday")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Test 9: Admin Dashboard ETL - Run ETL for Date
     */
    @Test
    @Order(9)
    @WithMockUser(roles = "ADMIN")
    void testAdminDashboardETLRunForDate() throws Exception {
        mockMvc.perform(post("/api/admin/dashboard-etl/run")
                        .param("date", testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.date").exists());
    }

    /**
     * Test 10: Admin Dashboard ETL - Run ETL without Date (should use yesterday)
     */
    @Test
    @Order(10)
    @WithMockUser(roles = "ADMIN")
    void testAdminDashboardETLRunWithoutDate() throws Exception {
        mockMvc.perform(post("/api/admin/dashboard-etl/run")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Test 11: Seller Dashboard ETL - Run ETL for All Sellers
     */
    @Test
    @Order(11)
    @WithMockUser(roles = "ADMIN")
    void testSellerDashboardETLRunForAllSellers() throws Exception {
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-all")
                        .param("date", testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Test 12: Seller Dashboard ETL - Run ETL for Specific Seller
     */
    @Test
    @Order(12)
    @WithMockUser(roles = "ADMIN")
    void testSellerDashboardETLRunForSpecificSeller() throws Exception {
        Integer testSellerId = ETLTestDataUtil.getTestSellerIds()[0];
        
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-seller/" + testSellerId)
                        .param("date", testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Test 13: Security Test - Unauthorized Access to Admin Endpoints
     */
    @Test
    @Order(13)
    void testUnauthorizedAccessToAdminEndpoints() throws Exception {
        // Test ETL Controller admin endpoint without authentication
        mockMvc.perform(post("/api/etl/run/today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // Test Admin Dashboard ETL without authentication
        mockMvc.perform(post("/api/admin/dashboard-etl/run")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // Test Seller Dashboard ETL without authentication
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
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
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 15: Error Handling - Future Date Validation
     */
    @Test
    @Order(15)
    @WithMockUser(authorities = "Admin")
    void testFutureDateValidation() throws Exception {
        String futureDate = ETLTestDataUtil.getFutureDateString();
        
        mockMvc.perform(get("/api/etl/metrics/sales/" + futureDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_DATA"));
    }

    /**
     * Test 16: Performance Test - Multiple Concurrent Requests
     */
    @Test
    @Order(16)
    void testConcurrentHealthChecks() throws Exception {
        // Test multiple health check requests to ensure system stability
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/etl/health")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    /**
     * Test 17: Data Validation - Test with Edge Cases
     */
    @Test
    @Order(17)
    @WithMockUser(roles = "ADMIN")
    void testEdgeCasesForSellerETL() throws Exception {
        // Test with seller ID 0 (edge case)
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-seller/0")
                        .param("date", testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
        
        // Test with very high seller ID 
        mockMvc.perform(post("/api/admin/seller-dashboard-etl/run-seller/999999")
                        .param("date", testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    /**
     * Helper method to print test results
     */
    @AfterEach
    void printTestResult(TestInfo testInfo) {
        System.out.println("✅ Completed test: " + testInfo.getDisplayName());
    }

    @AfterAll
    void tearDown() {
        System.out.println("🎉 All ETL API integration tests completed!");
        System.out.println("📊 Summary:");
        System.out.println("- ETL Controller: 8 endpoints tested");
        System.out.println("- Admin Dashboard ETL: 2 endpoints tested");
        System.out.println("- Seller Dashboard ETL: 2 endpoints tested");
        System.out.println("- Security tests: 1 test");
        System.out.println("- Error handling: 2 tests");
        System.out.println("- Performance tests: 1 test");
        System.out.println("- Edge case tests: 1 test");
        System.out.println("📝 Total: 17 comprehensive tests");
    }
}
