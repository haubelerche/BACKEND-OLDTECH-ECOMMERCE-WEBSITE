package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/refund")
public class RefundController {

    @Autowired
    private RefundService refundService;

    // Request a refund for an order
    @PostMapping("/requestRefund/{orderId}")
    public ResponseEntity<?> requestRefund(@RequestParam Integer orderId, @RequestParam String reason) {
        refundService.requestRefund(orderId, reason);
        return ResponseEntity.ok("Refund request submitted for order " + orderId + ".");
    }
}

