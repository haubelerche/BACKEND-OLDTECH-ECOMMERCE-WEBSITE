package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.PaymentRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "${frontend.url}")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);    @Autowired
    private CheckoutService checkoutService;@PostMapping("/createPaymentUrl")
    public ResponseEntity<?> createPaymentUrl(@RequestBody PaymentRequest request) {
        try {
            Map<String, Object> result = checkoutService.createPaymentUrl(request);
            boolean success = (Boolean) result.get("success");
            
            if (success) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            logger.error("Exception in createPaymentUrl", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Internal server error: " + e.getMessage()
            ));
        }
    }    @GetMapping("/handlePaymentReturn")
    public ResponseEntity<?> handlePaymentReturn(@RequestParam Map<String, String> response) {
        try {
            Map<String, Object> result = checkoutService.handlePaymentReturn(response);
            boolean success = (Boolean) result.get("success");
            
            if (success) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            logger.error("Exception in handlePaymentReturn", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Error processing payment response: " + e.getMessage(),
                "success", false
            ));
        }
    }


}