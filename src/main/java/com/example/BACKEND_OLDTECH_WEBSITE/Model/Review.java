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

    private Integer productId; //vẫn để productId ở đây vì nhận xét người bán, đính kèm theo ng mua đã mua gì của nó mà đánh giá nó thế. ok

    
    private Integer rating;


    private String comment;

    private Timestamp reviewTime;


    private String sellerResponse;

    private Timestamp responseTime;
}