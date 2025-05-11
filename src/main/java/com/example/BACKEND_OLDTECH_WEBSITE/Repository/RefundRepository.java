package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {
    List<Refund> findByStatus(RefundStatusEnum status);
    List<Refund> findByUserId(Integer userId);
    List<Refund> findByOrderId(Integer orderId);
}