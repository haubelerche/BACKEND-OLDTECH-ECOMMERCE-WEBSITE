package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Configuration.MomoPayment;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.PaymentRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoCreatePaymentResponseModel;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoExecuteResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "${frontend.url}")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private MomoPayment momoPayment;
    @PostMapping("/createPaymentUrl")
    public ResponseEntity<?> createPaymentUrl(@RequestBody PaymentRequest request) {
        try {
            logger.info("Creating MoMo payment for order: {}, amount: {}", request.getOrderId(), request.getAmount());
            
            MomoCreatePaymentResponseModel response = momoPayment.createPaymentRequest(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getOrderInfo()
            );
            
            if (response.getErrorCode() == 0) {
                // Success - return payment details instead of redirect
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Payment URL created successfully");
                result.put("payUrl", response.getPayUrl());
                result.put("qrCodeUrl", response.getQrCodeUrl());
                result.put("deeplink", response.getDeeplink());
                result.put("orderId", response.getOrderId());
                
                logger.info("MoMo payment URL created successfully for order: {}", request.getOrderId());
                return ResponseEntity.ok(result);
            } else {
                // Error from MoMo
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Failed to create payment URL: " + response.getMessage());
                result.put("errorCode", response.getErrorCode());
                
                logger.error("MoMo payment creation failed for order: {}, error: {}", 
                           request.getOrderId(), response.getMessage());
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            logger.error("Exception creating MoMo payment for order: " + request.getOrderId(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Internal error: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/handlePaymentReturn")
    public ResponseEntity<?> handlePaymentReturn(@RequestParam Map<String, String> response) {
        try {
            MomoExecuteResponseModel result = momoPayment.processPaymentResponse(response);
            if (result != null) {
                String resultCode = result.getResultCode();

                if ("0".equals(resultCode)) {
                    return ResponseEntity.ok(Map.of(
                            "status", "success",
                            "message", "Payment successful",
                            "orderId", result.getOrderId(),
                            "data", result
                    ));
                } else {
                    return ResponseEntity.ok(Map.of(
                            "status", "failed",
                            "message", "Payment failed",
                            "orderId", result.getOrderId(),
                            "resultCode", resultCode,
                            "data", result
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


}