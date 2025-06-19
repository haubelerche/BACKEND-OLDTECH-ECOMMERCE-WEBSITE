package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

/**
 * Simple test to debug ETL Controller mapping issues
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ETLControllerMappingTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void testApplicationStartup() {
        System.out.println("🚀 Application started on port: " + port);
        System.out.println("🔧 WebApplicationContext: " + webApplicationContext);
        
        // Print all beans related to ETL
        String[] beanNames = webApplicationContext.getBeanDefinitionNames();
        System.out.println("📋 Total beans: " + beanNames.length);
        
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("etl")) {
                System.out.println("🟢 ETL Bean found: " + beanName);
                Object bean = webApplicationContext.getBean(beanName);
                System.out.println("   Type: " + bean.getClass().getName());
            }
        }
    }

    @Test
    void testBasicEndpoint() {
        String baseUrl = "http://localhost:" + port;
        System.out.println("🌐 Testing base URL: " + baseUrl);
        
        // Test root endpoint
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/", String.class);
            System.out.println("📊 Root endpoint status: " + response.getStatusCode());
        } catch (Exception e) {
            System.out.println("⚠️ Root endpoint error: " + e.getMessage());
        }
        
        // Test actuator health if available
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/actuator/health", String.class);
            System.out.println("📊 Actuator health status: " + response.getStatusCode());
            System.out.println("📋 Health response: " + response.getBody());
        } catch (Exception e) {
            System.out.println("⚠️ Actuator health not available: " + e.getMessage());
        }
    }

    @Test
    void testETLHealthEndpoint() {
        String baseUrl = "http://localhost:" + port;
        String[] etlEndpoints = {
            "/api/etl/health",
            "/api/etl/info", 
            "/api/etl/status",
            "/api/etl/metrics/latest"
        };
        
        System.out.println("🔍 Testing ETL endpoints:");
        for (String endpoint : etlEndpoints) {
            try {
                String url = baseUrl + endpoint;
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                System.out.println("✅ " + endpoint + " -> " + response.getStatusCode());                String body = response.getBody();
                if (body != null && body.length() < 200) {
                    System.out.println("    Body: " + body);
                }
            } catch (Exception e) {
                System.out.println("❌ " + endpoint + " -> Error: " + e.getMessage());
            }
        }
    }

    @Test 
    void testControllerBeanExistence() {
        System.out.println("🔍 Checking for ETL Controller beans:");
        
        try {
            Object etlController = webApplicationContext.getBean("ETLController");
            System.out.println("✅ ETLController bean found: " + etlController.getClass().getName());
        } catch (Exception e) {
            System.out.println("❌ ETLController bean not found: " + e.getMessage());
        }
        
        // Check by type
        try {
            String[] beanNames = webApplicationContext.getBeanNamesForType(
                Class.forName("com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller.ETLController")
            );
            System.out.println("📋 ETLController beans by type: " + java.util.Arrays.toString(beanNames));
        } catch (Exception e) {
            System.out.println("⚠️ Could not check ETLController by type: " + e.getMessage());
        }
    }
}
