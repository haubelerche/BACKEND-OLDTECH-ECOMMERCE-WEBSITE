package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_kpi_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminKPIMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    private MetricType metricType = MetricType.daily;
    
    // Financial KPIs
    @Column(name = "gmv", precision = 15, scale = 2)
    private BigDecimal gmv = BigDecimal.ZERO;
    
    @Column(name = "aov", precision = 10, scale = 2)
    private BigDecimal aov = BigDecimal.ZERO;
    
    @Column(name = "platform_revenue", precision = 15, scale = 2)
    private BigDecimal platformRevenue = BigDecimal.ZERO;
      @Column(name = "total_orders")
    private Integer totalOrders = 0;
    
    // User Metrics
    @Column(name = "total_visits")
    private Integer totalVisits = 0;
      @Column(name = "new_users")
    private Integer newUsers = 0;
      @Column(name = "conversion_rate", precision = 5, scale = 4)
    private BigDecimal conversionRate = BigDecimal.ZERO;
    
    @Column(name = "returning_customers_rate", precision = 5, scale = 4) 
    private BigDecimal returningCustomersRate = BigDecimal.ZERO;
    
    // Seller/Product Metrics
    @Column(name = "pending_sellers")
    private Integer pendingSellers = 0;
    
    @Column(name = "approved_sellers")
    private Integer approvedSellers = 0;
    
    @Column(name = "rejected_sellers")
    private Integer rejectedSellers = 0;
    
    @Column(name = "pending_products")
    private Integer pendingProducts = 0;
    
    @Column(name = "active_products")
    private Integer activeProducts = 0;
    
    // Geographic and Performance
    @Column(name = "top_province", length = 100)
    private String topProvince;
    
    @Column(name = "suspicious_transactions")
    private Integer suspiciousTransactions = 0;
    
    @Column(name = "new_complaints")
    private Integer newComplaints = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum MetricType {
        daily, weekly, monthly, yearly
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
