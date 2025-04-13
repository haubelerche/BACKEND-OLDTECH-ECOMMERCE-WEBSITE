package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;

import lombok.*;

import java.sql.Timestamp;


@Entity
@Table(name = "review")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", nullable = false)
    private Integer reviewId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order orderId;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewerId;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User sellerId;

    @Column(name = "rating", nullable = false)
    
    private Integer rating;

    @Column(name = "comment", columnDefinition = "MEDIUMTEXT")
    
    private String comment;

    @Column(name = "review_time", nullable = false)
    private Timestamp reviewTime;

    @Column(name = "seller_response", columnDefinition = "MEDIUMTEXT")
    
    private String sellerResponse;

    @Column(name = "response_time", nullable = false)
    private Timestamp responseTime;
}