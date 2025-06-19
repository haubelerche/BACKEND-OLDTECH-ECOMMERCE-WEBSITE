package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Integer> {
    // Basic queries using method naming convention
    List<Orders> findByUserId(Integer userId);
    
    // Find orders by status
    List<Orders> findByStatus(OrderStatusEnum status);
    
    // Find orders by user and status
    List<Orders> findByUserIdAndStatus(Integer userId, OrderStatusEnum status);
    
    // Find orders by status and delivered time not null and delivered time before given time
    List<Orders> findByStatusAndDeliveredAtIsNotNullAndDeliveredAtBefore(
        OrderStatusEnum status, Timestamp beforeTime);
    
    // Find orders delivered but not completed within time range
    List<Orders> findByStatusAndDeliveredAtBetween(
        OrderStatusEnum status, Timestamp startTime, Timestamp endTime);
    
    // Find orders by status and delivered time not null (for monitoring)
    List<Orders> findByStatusAndDeliveredAtIsNotNull(OrderStatusEnum status);
}
