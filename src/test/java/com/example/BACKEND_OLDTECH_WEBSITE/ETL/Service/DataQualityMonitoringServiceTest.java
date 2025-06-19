package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for data quality monitoring and validation
 */
@ExtendWith(MockitoExtension.class)
class DataQualityMonitoringServiceTest {

    @InjectMocks
    private DataQualityMonitoringService dataQualityService;    private ExtractedData extractedData;

    @BeforeEach
    void setUp() {
        extractedData = createMockExtractedData();
    }

    @Test
    void testValidateDataQuality_ValidData() {
        // Given: Valid extracted data
        extractedData.setOrders(createValidOrders());

        // When: Validate data quality
        assertDoesNotThrow(() -> {
            dataQualityService.validateDataQuality(extractedData);
        });

        // Then: Quality report should be generated
        assertNotNull(extractedData.getQualityReport());
        assertTrue(extractedData.getQualityReport().getQualityScore() > 80.0);
    }

    @Test
    void testValidateDataQuality_EmptyData() {
        // Given: Empty extracted data
        extractedData.setOrders(new ArrayList<>());
        extractedData.setProductMetrics(new ArrayList<>());

        // When: Validate data quality
        dataQualityService.validateDataQuality(extractedData);

        // Then: Should detect low data volume
        assertNotNull(extractedData.getQualityReport());
        // Quality score should be lower for empty data
        assertTrue(extractedData.getQualityReport().getQualityScore() < 100.0);
    }    @Test
    void testValidateDataQuality_InvalidData() {
        // Given: Data with null/invalid values
        List<ExtractedData.OrderData> invalidOrders = new ArrayList<>();
        ExtractedData.OrderData invalidOrder = new ExtractedData.OrderData();
        invalidOrder.setOrderId(null); // Invalid: null order ID
        invalidOrder.setTotalAmount(BigDecimal.valueOf(-100)); // Invalid: negative value
        invalidOrders.add(invalidOrder);
        
        extractedData.setOrders(invalidOrders);

        // When: Validate data quality
        dataQualityService.validateDataQuality(extractedData);

        // Then: Should detect data quality issues
        assertNotNull(extractedData.getQualityReport());
        assertTrue(extractedData.getQualityReport().getQualityScore() < 50.0);
    }

    @Test
    void testGenerateDataQualityAlerts() {
        // Given: Data with quality issues
        extractedData.setOrders(createOrdersWithQualityIssues());

        // When: Generate quality alerts
        dataQualityService.validateDataQuality(extractedData);

        // Then: Should generate appropriate alerts
        var qualityReport = extractedData.getQualityReport();
        assertNotNull(qualityReport);
        assertFalse(qualityReport.getQualityIssues().isEmpty());        
        // Check for specific alert types
        assertTrue(qualityReport.getQualityIssues().stream()
            .anyMatch(alert -> alert.contains("volume") || alert.contains("count")));
        
        assertTrue(qualityReport.getQualityIssues().stream()
            .anyMatch(alert -> alert.contains("invalid") || alert.contains("null")));
    }

    private ExtractedData createMockExtractedData() {
        ExtractedData data = new ExtractedData();
        data.setExtractionDate(LocalDate.now());
        data.setOrders(new ArrayList<>());
        data.setProductMetrics(new ArrayList<>());
        data.setCustomerActivities(new ArrayList<>());
        data.setSellerMetrics(new ArrayList<>());
        return data;
    }

    private List<ExtractedData.OrderData> createValidOrders() {
        List<ExtractedData.OrderData> orders = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ExtractedData.OrderData order = new ExtractedData.OrderData();
            order.setOrderId(i);
            order.setUserId(i);
            order.setTotalAmount(BigDecimal.valueOf(100.0 * i));
            order.setStatus("COMPLETED");
            order.setPaymentMethod("CREDIT_CARD");
            order.setOrderDate(LocalDate.now());
            orders.add(order);
        }
        return orders;
    }

    private List<ExtractedData.OrderData> createOrdersWithQualityIssues() {
        List<ExtractedData.OrderData> orders = new ArrayList<>();
        
        // Add some valid orders
        orders.addAll(createValidOrders());
        
        // Add orders with quality issues
        ExtractedData.OrderData badOrder1 = new ExtractedData.OrderData();
        badOrder1.setOrderId(null); // Missing order ID
        badOrder1.setTotalAmount(BigDecimal.valueOf(50.0));
        orders.add(badOrder1);
        
        ExtractedData.OrderData badOrder2 = new ExtractedData.OrderData();
        badOrder2.setOrderId(100);
        badOrder2.setTotalAmount(BigDecimal.valueOf(-25.0)); // Negative value
        orders.add(badOrder2);
        
        return orders;
    }
}
