package com.example.BACKEND_OLDTECH_WEBSITE.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
public class MomoPayment {
    
    @Value("${momo.partnerCode}")
    private String partnerCode;
    
    @Value("${momo.accessKey}")
    private String accessKey;
    
    @Value("${momo.secretKey}")
    private String secretKey;
    
    @Value("${momo.endpoint}")
    private String endpoint;
    
    @Value("${momo.returnUrl}")
    private String returnUrl;
    
    @Value("${momo.ipnUrl}")
    private String ipnUrl;

    public String createPaymentRequest(String orderId, long amount, String orderInfo) {
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("partnerCode", partnerCode);
            parameters.put("requestId", UUID.randomUUID().toString());
            parameters.put("amount", String.valueOf(amount));
            parameters.put("orderId", orderId);
            parameters.put("orderInfo", orderInfo);
            parameters.put("redirectUrl", returnUrl);
            parameters.put("ipnUrl", ipnUrl);
            parameters.put("extraData", "");
            parameters.put("requestType", "captureWallet");
            
            String signature = generateSignature(parameters);
            parameters.put("signature", signature);
            
            // Here you would make the HTTP request to MOMO's API
            // For now, we'll return the parameters as a string
            return parameters.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating MOMO payment request", e);
        }
    }

    private String generateSignature(Map<String, String> parameters) throws NoSuchAlgorithmException {
        StringBuilder data = new StringBuilder();
        data.append("partnerCode=").append(parameters.get("partnerCode"))
            .append("&accessKey=").append(accessKey)
            .append("&requestId=").append(parameters.get("requestId"))
            .append("&amount=").append(parameters.get("amount"))
            .append("&orderId=").append(parameters.get("orderId"))
            .append("&orderInfo=").append(parameters.get("orderInfo"))
            .append("&redirectUrl=").append(parameters.get("redirectUrl"))
            .append("&ipnUrl=").append(parameters.get("ipnUrl"))
            .append("&extraData=").append(parameters.get("extraData"));

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(data.toString().getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    public boolean verifyPaymentResponse(Map<String, String> response) {
        try {
            String signature = response.get("signature");
            response.remove("signature");
            
            String calculatedSignature = generateSignature(response);
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            return false;
        }
    }
}
