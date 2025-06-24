package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/refund")
public class RefundController {

    @Autowired
    private RefundService refundService;
    @PreAuthorize("hasAuthority('Customer')")
    
    @PostMapping("/requestRefund")
    public ResponseEntity<?> requestRefund(@RequestParam Integer orderId, @RequestParam String reason) {
        refundService.requestRefund(orderId, reason);
        return ResponseEntity.ok("Khiếu nại của bạn đã được gửi đi " + orderId + ".");
    }
}

