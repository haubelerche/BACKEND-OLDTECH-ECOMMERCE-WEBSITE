package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.config.ETLTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test ETL APIs với mock services để tránh dependency issues
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ETLTestConfiguration.class)
class ETLControllerWithMocksTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testETLHealthEndpointWithMocks() {
        System.out.println("🔍 Testing ETL Health with mocked services");
        
        String url = getBaseUrl() + "/api/etl/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        System.out.println("URL: " + url);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        // With mocked services, should not get 500 error
        assertNotEquals(500, response.getStatusCode().value(), 
                       "Should not get server error with mocked services");
    }

    @Test
    void testAllETLEndpointsWithMocks() {
        String[] endpoints = {
            "/api/etl/health",
            "/api/etl/info",
            "/api/etl/status",
            "/api/etl/metrics/latest",
            "/api/etl/metrics/sales/2025-06-19"
        };
        
        System.out.println("🔍 Testing all ETL endpoints with mocked services:");
        
        for (String endpoint : endpoints) {
            String url = getBaseUrl() + endpoint;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            System.out.println("  " + endpoint + " -> " + response.getStatusCode());
            
            // Should not get 500 errors with mocked services
            assertNotEquals(500, response.getStatusCode().value(), 
                           "Endpoint " + endpoint + " should not return server error");
        }
    }

    @Test
    void testApplicationContext() {
        System.out.println("🔍 Testing application context loads properly");
        System.out.println("Port: " + port);
        System.out.println("RestTemplate: " + (restTemplate != null ? "✅ Injected" : "❌ Null"));
        
        assertTrue(port > 0, "Port should be assigned");
        assertNotNull(restTemplate, "RestTemplate should be injected");
    }
}
