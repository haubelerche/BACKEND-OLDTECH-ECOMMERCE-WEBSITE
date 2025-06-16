package com.example.BACKEND_OLDTECH_WEBSITE.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfiguration implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configure static resource handling for uploaded files
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600) // Cache for 1 hour
                .resourceChain(true);

        // Set order to ensure controller mappings take precedence
        registry.setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Ensure REST controllers have higher priority than static resources
        configurer.setUseTrailingSlashMatch(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Give REST endpoints higher priority
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
