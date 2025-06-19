package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDTO {
    private String label;
    private String type; // "line", "bar", "pie", etc.
    private List<ChartPointDTO> data;
    private String timeRange; // "daily", "weekly", "monthly"
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPointDTO {
        private String label; // date or category name
        private Double value;
        private LocalDateTime timestamp;
    }
}
