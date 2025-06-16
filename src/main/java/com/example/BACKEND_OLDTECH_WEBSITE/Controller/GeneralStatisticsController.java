package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Service.GeneralStatisticsService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.SellerService;
import jakarta.persistence.EntityNotFoundException;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics.WebsiteStatisticsDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics.ProductStatisticsDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics.RevenueStatisticsDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics.UserStatisticsDTO;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/statistics")
public class GeneralStatisticsController {

    @Autowired
    private GeneralStatisticsService statisticsService;
    @Autowired
    private SellerService sellerService;





/*--- PHẦN THỐNG KÊ DÀNH CHO ADMIN ---*/

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




/*--- PHẦN THỐNG KÊ DÀNH CHO NGƯỜI BÁN ---*/
    @GetMapping("/statistics/revenue/{sellerId}")
    public ResponseEntity<?> getRevenueStatistics(@PathVariable Integer sellerId) {
        try {
            Map<String, Object> statistics = sellerService.getRevenueStatistics(sellerId);
            return ResponseEntity.ok(statistics);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy thống kê doanh thu: " + e.getMessage());
        }
    }
}

