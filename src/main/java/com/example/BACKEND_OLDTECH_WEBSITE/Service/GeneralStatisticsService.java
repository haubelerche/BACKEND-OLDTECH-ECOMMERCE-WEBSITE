package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics.*;
import org.springframework.stereotype.Service;

@Service
public class GeneralStatisticsService {
    public WebsiteStatisticsDTO getWebsiteStatistics() {
        return new WebsiteStatisticsDTO();
    }
    public ProductStatisticsDTO getProductStatistics() {
        return new ProductStatisticsDTO();
    }
    public RevenueStatisticsDTO getRevenueStatistics() {
        return new RevenueStatisticsDTO();
    }
    public UserStatisticsDTO getUserStatistics() {
        return new UserStatisticsDTO();
    }
}

//havent tested yet