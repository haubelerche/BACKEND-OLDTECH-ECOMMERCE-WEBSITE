package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatisticsDTO {
    private double addToCartRate;
    private double cartAbandonmentRate;
    private double unitsPerTransaction; // UPT
    private BigDecimal averageOrderValue; // AOV
    private long totalProductsSold;
    private long totalViews;
    // You can add more specific product statistics fields here
    // e.g., Map<String, Long> topSellingProducts; (Product Name, Quantity Sold)
    // e.g., Map<String, Double> categoryPerformance; (Category Name, Revenue)
} 