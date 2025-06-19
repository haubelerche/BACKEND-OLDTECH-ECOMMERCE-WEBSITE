package com.example.BACKEND_OLDTECH_WEBSITE.ETL.config;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.ETLOrchestrator;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.DataLoaderService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * Test configuration để mock ETL services cho testing
 */
@TestConfiguration
public class ETLTestConfiguration {

    @Bean
    @Primary
    public ETLOrchestrator etlOrchestrator() {
        return mock(ETLOrchestrator.class);
    }

    @Bean
    @Primary
    public DataLoaderService dataLoaderService() {
        return mock(DataLoaderService.class);
    }
}
