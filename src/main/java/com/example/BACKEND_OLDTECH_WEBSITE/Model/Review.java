package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Timestamp;


@Entity
@Table(name = "review")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order orderId;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewerId;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User sellerId;

    @Column(name = "rating")
    @NotBlank(message = "Thông tin không được để trống")
    private Integer rating;

    @Column(name = "comment", columnDefinition = "MEDIUMTEXT")
    @NotBlank(message = "Thông tin không được để trống")
    private String comment;

    @Column(name = "review_time")
    private Timestamp reviewTime;

    @Column(name = "seller_response", columnDefinition = "MEDIUMTEXT")
    @NotBlank(message = "Thông tin không được để trống")
    private String sellerResponse;

    @Column(name = "response_time")
    private Timestamp responseTime;
}