package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model.ExtractedData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * kiểm tra chất lượng dữ liệu và tạo báo cáo chất lượng
 * 
 */
@Service
@Slf4j
public class DataQualityMonitoringService {

    /**
     * kiểm tra chất lượng dữ liệu của dữ liệu đã trích xuất
     */
    public void validateDataQuality(ExtractedData extractedData) {
        log.info("Starting data quality validation");
        
        ExtractedData.DataQualityReport report = extractedData.getQualityReport();
        
        if (report.getQualityScore() < 70.0) {
            log.warn("Data quality score is below acceptable threshold: {}%", report.getQualityScore());
        }
        
        if (!report.getQualityIssues().isEmpty()) {
            log.warn("Data quality issues found: {}", report.getQualityIssues());
        }
        
        log.info("Data quality validation completed. Score: {}%", report.getQualityScore());
    }
}
