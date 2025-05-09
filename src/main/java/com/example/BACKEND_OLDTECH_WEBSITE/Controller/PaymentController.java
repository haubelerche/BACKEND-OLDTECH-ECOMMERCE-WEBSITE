package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Configuration.MomoPayment;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "${frontend.url}")
public class PaymentController {
    
    @Autowired
    private MomoPayment momoPayment;
    
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
        try {
            String paymentUrl = momoPayment.createPaymentRequest(
                request.getOrderId(),
                request.getAmount(),
                request.getOrderInfo()
            );
            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating payment: " + e.getMessage());
        }
    }
    
    @PostMapping("/return")
    public ResponseEntity<?> handlePaymentReturn(@RequestParam Map<String, String> response) {
        try {
            if (momoPayment.verifyPaymentResponse(response)) {
                // Handle successful payment
                String orderId = response.get("orderId");
                String resultCode = response.get("resultCode");
                
                if ("0".equals(resultCode)) {
                    return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Payment successful",
                        "orderId", orderId
                    ));
                } else {
                    return ResponseEntity.ok(Map.of(
                        "status", "failed",
                        "message", "Payment failed",
                        "orderId", orderId,
                        "resultCode", resultCode
                    ));
                }
            }
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Invalid payment response"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Error processing payment response: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/ipn")
    public ResponseEntity<?> handleIPN(@RequestParam Map<String, String> response) {
        try {
            if (momoPayment.verifyPaymentResponse(response)) {
                // Process IPN (Instant Payment Notification)
                String orderId = response.get("orderId");
                String resultCode = response.get("resultCode");
                
                // Here you would typically update your database with the payment status
                // and perform any necessary business logic
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "IPN processed successfully",
                    "orderId", orderId
                ));
            }
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Invalid IPN response"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Error processing IPN: " + e.getMessage()
            ));
        }
    }
} 