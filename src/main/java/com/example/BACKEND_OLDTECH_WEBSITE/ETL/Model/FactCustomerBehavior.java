package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bảng phân tích hành vi khách hàng
 */
@Entity
@Table(name = "fact_customer_behavior")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactCustomerBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_key", nullable = false)
    private LocalDate dateKey;

    @Column(name = "customer_segment")
    private String customerSegment;
    
   @Column(name = "customer_count")
    private Integer customerCount;

    @Column(name = "avg_order_value", precision = 10, scale = 2)
    private BigDecimal avgOrderValue;

    @Column(name = "conversion_rate", precision = 5, scale = 4)
    private BigDecimal conversionRate;

    @Column(name = "retention_rate", precision = 5, scale = 4)
    private BigDecimal retentionRate;

    @Column(name = "lifetime_value", precision = 10, scale = 2)
    private BigDecimal lifetimeValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
