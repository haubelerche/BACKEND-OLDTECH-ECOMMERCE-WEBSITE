package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository.ETLOrderRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Repository.ETLProductRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataExtractorService
 * Tests data extraction logic in isolation
 */
@ExtendWith(MockitoExtension.class)
class DataExtractorServiceTest {

    @Mock
    private ETLOrderRepository etlOrderRepository;
    
    @Mock
    private ETLProductRepository etlProductRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private DataExtractorService dataExtractorService;

    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);
    }

    @Test
    void testExtractDailyData_Success() {
        // Given: Mock repository responses
        when(etlOrderRepository.findOrdersByDateRange(any(), any())).thenReturn(new ArrayList<>());
        when(etlProductRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(sellerRepository.findAll()).thenReturn(new ArrayList<>());

        // When: Extract data for test date
        ExtractedData result = dataExtractorService.extractDailyData(testDate);

        // Then: Verify extraction completed successfully
        assertNotNull(result);
        assertEquals(testDate, result.getExtractionDate());
        assertNotNull(result.getOrders());
        assertNotNull(result.getProductMetrics());
        assertNotNull(result.getCustomerActivities());
        assertNotNull(result.getSellerMetrics());
        assertNotNull(result.getQualityReport());

        // Verify all repositories were called
        verify(etlOrderRepository, times(2)).findOrdersByDateRange(any(), any());
        verify(etlProductRepository).findAll();
        verify(userRepository).findAll();
        verify(sellerRepository).findAll();
    }

    @Test
    void testExtractDailyData_ExceptionHandling() {
        // Given: Repository throws exception
        when(etlOrderRepository.findOrdersByDateRange(any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then: Verify exception is handled properly
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dataExtractorService.extractDailyData(testDate);
        });

        assertTrue(exception.getMessage().contains("Trích xuất dữ liệu thất bại cho ngày"));
    }

    @Test
    void testExtractDailyData_EmptyDataHandling() {
        // Given: All repositories return empty data
        when(etlOrderRepository.findOrdersByDateRange(any(), any())).thenReturn(new ArrayList<>());
        when(etlProductRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(sellerRepository.findAll()).thenReturn(new ArrayList<>());

        // When: Extract data
        ExtractedData result = dataExtractorService.extractDailyData(testDate);

        // Then: Verify empty collections are handled properly
        assertNotNull(result);
        assertTrue(result.getOrders().isEmpty());
        assertTrue(result.getProductMetrics().isEmpty());
        assertTrue(result.getCustomerActivities().isEmpty());
        assertTrue(result.getSellerMetrics().isEmpty());
    }
}
