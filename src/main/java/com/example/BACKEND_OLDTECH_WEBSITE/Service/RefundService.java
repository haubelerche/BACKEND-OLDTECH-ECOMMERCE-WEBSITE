package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Refund;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.RefundRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefundService {
    @Autowired
    private RefundRepository refundRepository;

    public void requestRefund(Integer orderId, String reason) {
        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setReason(reason);
        refund.setStatus(RefundStatusEnum.Pending);
        refundRepository.save(refund);
    }
}//need help