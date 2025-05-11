package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Service.GeneralStatisticsService;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics.WebsiteStatisticsDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics.ProductStatisticsDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics.RevenueStatisticsDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics.UserStatisticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/statistics")
public class GeneralStatisticsController {

    @Autowired
    private GeneralStatisticsService statisticsService;

    // Get overall website statistics (traffic, CAC, conversion, etc.)
    @GetMapping("/website")
    public ResponseEntity<WebsiteStatisticsDTO> getWebsiteStatistics() {
        WebsiteStatisticsDTO stats = statisticsService.getWebsiteStatistics();
        return ResponseEntity.ok(stats);
    }

    // Get product-related statistics (add-to-cart rate, abandonment, UPT, AOV, etc.)
    @GetMapping("/product")
    public ResponseEntity<ProductStatisticsDTO> getProductStatistics() {
        ProductStatisticsDTO stats = statisticsService.getProductStatistics();
        return ResponseEntity.ok(stats);
    }

    // Get revenue statistics (total, by period, etc.)
    @GetMapping("/revenue")
    public ResponseEntity<RevenueStatisticsDTO> getRevenueStatistics() {
        RevenueStatisticsDTO stats = statisticsService.getRevenueStatistics();
        return ResponseEntity.ok(stats);
    }

    // Get user-related statistics (return rate, repeat customers, etc.)
    @GetMapping("/user")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics() {
        UserStatisticsDTO stats = statisticsService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }
}