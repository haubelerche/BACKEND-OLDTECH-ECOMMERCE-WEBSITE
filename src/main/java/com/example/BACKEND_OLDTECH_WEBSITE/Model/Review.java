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

    private Integer reviewId;


    private Integer orderId;


    private Integer reviewerId;


    private Integer sellerId;

    
    private Integer rating;


    private String comment;

    private Timestamp reviewTime;


    private String sellerResponse;

    private Timestamp responseTime;
}