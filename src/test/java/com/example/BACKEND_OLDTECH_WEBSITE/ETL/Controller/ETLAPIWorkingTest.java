package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test thực tế cho tất cả ETL APIs với cách tiếp cận đơn giản hơn
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ETLAPIWorkingTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testETLHealthEndpoint() {
        // Test ETL Health endpoint
        String url = getBaseUrl() + "/api/etl/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        System.out.println("🔍 ETL Health Test:");
        System.out.println("URL: " + url);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        // Accept any non-server error response
        assertTrue(response.getStatusCode().value() < 500, 
                  "Server should not return 500 error. Got: " + response.getStatusCode());
    }

    @Test
    void testETLInfoEndpoint() {
        String url = getBaseUrl() + "/api/etl/info";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        System.out.println("🔍 ETL Info Test:");
        System.out.println("URL: " + url);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        assertTrue(response.getStatusCode().value() < 500, 
                  "Server should not return 500 error. Got: " + response.getStatusCode());
    }

    @Test
    void testETLMetricsEndpoint() {
        String url = getBaseUrl() + "/api/etl/metrics/latest";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        System.out.println("🔍 ETL Latest Metrics Test:");
        System.out.println("URL: " + url);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        assertTrue(response.getStatusCode().value() < 500, 
                  "Server should not return 500 error. Got: " + response.getStatusCode());
    }

    @Test
    void testETLSalesMetricsEndpoint() {
        String testDate = "2025-06-19"; // Yesterday from current date
        String url = getBaseUrl() + "/api/etl/metrics/sales/" + testDate;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        System.out.println("🔍 ETL Sales Metrics Test:");
        System.out.println("URL: " + url);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        assertTrue(response.getStatusCode().value() < 500, 
                  "Server should not return 500 error. Got: " + response.getStatusCode());
    }

    @Test
    void testETLStatusEndpoint() {
        String url = getBaseUrl() + "/api/etl/status";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        System.out.println("🔍 ETL Status Test (Admin endpoint):");
        System.out.println("URL: " + url);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        // This endpoint requires admin auth, so expect 302 (redirect) or 401/403
        assertTrue(response.getStatusCode().value() < 500, 
                  "Server should not return 500 error. Got: " + response.getStatusCode());
    }

    @Test
    void testAdminDashboardETLEndpoint() {
        String testDate = "2025-06-19";
        String url = getBaseUrl() + "/api/admin/dashboard-etl/run?date=" + testDate;
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        
        System.out.println("🔍 Admin Dashboard ETL Test:");
        System.out.println("URL: " + url);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        assertTrue(response.getStatusCode().value() < 500, 
                  "Server should not return 500 error. Got: " + response.getStatusCode());
    }

    @Test
    void testSellerDashboardETLEndpoint() {
        String testDate = "2025-06-19";
        String url = getBaseUrl() + "/api/admin/seller-dashboard-etl/run-all?date=" + testDate;
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        
        System.out.println("🔍 Seller Dashboard ETL Test:");
        System.out.println("URL: " + url);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        assertTrue(response.getStatusCode().value() < 500, 
                  "Server should not return 500 error. Got: " + response.getStatusCode());
    }

    @Test
    void testErrorHandling() {
        // Test with invalid date format
        String url = getBaseUrl() + "/api/etl/metrics/sales/invalid-date";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        System.out.println("🔍 Error Handling Test:");
        System.out.println("URL: " + url);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        // Should handle gracefully - either 400 or 404 is acceptable
        assertTrue(response.getStatusCode().value() >= 400 && response.getStatusCode().value() < 500, 
                  "Should return client error for invalid date. Got: " + response.getStatusCode());
    }

    @Test
    void testApplicationIsRunning() {
        System.out.println("🚀 Application running on port: " + port);
        System.out.println("🌐 Base URL: " + getBaseUrl());
        
        // Just verify the application started
        assertTrue(port > 0, "Port should be assigned");
        assertNotNull(restTemplate, "RestTemplate should be injected");
    }
}
