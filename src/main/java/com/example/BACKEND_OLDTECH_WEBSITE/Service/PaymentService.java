package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Configuration.MomoPayment;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.PaymentRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoCreatePaymentResponseModel;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoExecuteResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private MomoPayment momoPayment;

    public Map<String, Object> createPaymentUrl(PaymentRequest request) {
        try {
            logger.info("Creating MoMo payment for order: {}, amount: {}", request.getOrderId(), request.getAmount());
            
            MomoCreatePaymentResponseModel response = momoPayment.createPaymentRequest(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getOrderInfo()
            );
            
            Map<String, Object> result = new HashMap<>();
            
            if (response.getErrorCode() == 0) {
                // Success - return payment details
                result.put("success", true);
                result.put("message", "Payment URL created successfully");
                result.put("payUrl", response.getPayUrl());
                result.put("qrCodeUrl", response.getQrCodeUrl());
                result.put("deeplink", response.getDeeplink());
                result.put("orderId", response.getOrderId());
                
                logger.info("MoMo payment URL created successfully for order: {}", request.getOrderId());
            } else {
                // Error from MoMo
                result.put("success", false);
                result.put("message", "Failed to create payment URL: " + response.getMessage());
                result.put("errorCode", response.getErrorCode());
                
                logger.error("MoMo payment creation failed for order: {}, error: {}", 
                           request.getOrderId(), response.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Exception creating MoMo payment for order: " + request.getOrderId(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Internal error: " + e.getMessage());
            
            return result;
        }
    }

    public Map<String, Object> handlePaymentReturn(Map<String, String> response) {
        try {
            MomoExecuteResponseModel result = momoPayment.processPaymentResponse(response);
            Map<String, Object> resultMap = new HashMap<>();
            
            if (result != null) {
                String resultCode = result.getResultCode();

                if ("0".equals(resultCode)) {
                    resultMap.put("status", "success");
                    resultMap.put("message", "Payment successful");
                    resultMap.put("orderId", result.getOrderId());
                    resultMap.put("data", result);
                    resultMap.put("success", true);
                } else {
                    resultMap.put("status", "failed");
                    resultMap.put("message", "Payment failed");
                    resultMap.put("orderId", result.getOrderId());
                    resultMap.put("resultCode", resultCode);
                    resultMap.put("data", result);
                    resultMap.put("success", false);
                }
            } else {
                resultMap.put("status", "error");
                resultMap.put("message", "Invalid payment response");
                resultMap.put("success", false);
            }

            return resultMap;
            
        } catch (Exception e) {
            logger.error("Error processing payment response", e);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("status", "error");
            resultMap.put("message", "Error processing payment response: " + e.getMessage());
            resultMap.put("success", false);
            return resultMap;
        }
    }
}
