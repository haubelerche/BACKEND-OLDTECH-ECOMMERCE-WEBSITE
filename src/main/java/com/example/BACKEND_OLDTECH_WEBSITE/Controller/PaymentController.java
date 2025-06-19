package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Configuration.MomoPayment;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.PaymentRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoCreatePaymentResponseModel;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoExecuteResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "${frontend.url}")
public class PaymentController {

    @Autowired
    private MomoPayment momoPayment;


    @PostMapping("/createPaymentUrl")
    public RedirectView createPaymentUrl(@RequestBody PaymentRequest request) {
        try {
            MomoCreatePaymentResponseModel response = momoPayment.createPaymentRequest(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getOrderInfo()
            );
            // Redirect to Momo payment page
            return new RedirectView(response.getPayUrl());
        } catch (Exception e) {
            // Handle error - redirect to error page
            return new RedirectView("/payment/error?message=" + e.getMessage());
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