package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {
    private long totalUsers;
    private long newUsers;
    private long activeUsers;
    private double customerReturnRate; // Percentage of customers who make a repeat purchase
    private long repeatCustomers;
    private double averageCustomerLifetimeValue; // CLV or LTV
    // You can add more specific user statistics fields here
    // e.g., Map<String, Long> userDemographics; (e.g., by region, age group)
} 