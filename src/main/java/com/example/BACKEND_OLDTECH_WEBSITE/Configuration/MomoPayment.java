package com.example.BACKEND_OLDTECH_WEBSITE.Configuration;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoCreatePaymentResponseModel;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoExecuteResponseModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MomoPayment {

    private final Logger logger = LoggerFactory.getLogger(MomoPayment.class);

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

    private final RestTemplate restTemplate = new RestTemplate();

    public MomoCreatePaymentResponseModel createPaymentRequest(String orderId, long amount, String orderInfo) {
        try {
            // Log configuration values to check if they're loaded correctly
            logger.info("Using Momo configuration: partnerCode={}, endpoint={}, returnUrl={}, ipnUrl={}",
                        partnerCode, endpoint, returnUrl, ipnUrl);

            // Create request parameters
            String requestId = UUID.randomUUID().toString();
            String extraData = "";
            String requestType = "captureWallet";

            // Build the raw signature string
            String rawSignature = "accessKey=" + accessKey +
                                 "&amount=" + amount +
                                 "&extraData=" + extraData +
                                 "&ipnUrl=" + ipnUrl +
                                 "&orderId=" + orderId +
                                 "&orderInfo=" + orderInfo +
                                 "&partnerCode=" + partnerCode +
                                 "&redirectUrl=" + returnUrl +
                                 "&requestId=" + requestId +
                                 "&requestType=" + requestType;

            // Calculate HMAC-SHA256 signature
            String signature = signHmacSHA256(rawSignature, secretKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", returnUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType);
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");

            logger.info("Sending payment request to Momo: {}", requestBody);

            // Configure HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Send HTTP request to MOMO API
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);
            logger.info("Received response from Momo: {}", response.getBody());

            // Extract response data and create response model
            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                MomoCreatePaymentResponseModel responseModel = new MomoCreatePaymentResponseModel();

                // Set properties from response
                responseModel.setRequestId((String) responseBody.get("requestId"));
                responseModel.setErrorCode(responseBody.get("errorCode") != null ?
                    Integer.parseInt(responseBody.get("errorCode").toString()) : 0);
                responseModel.setOrderId((String) responseBody.get("orderId"));
                responseModel.setMessage((String) responseBody.get("message"));
                responseModel.setLocalMessage((String) responseBody.get("localMessage"));
                responseModel.setRequestType((String) responseBody.get("requestType"));
                responseModel.setPayUrl((String) responseBody.get("payUrl"));
                responseModel.setSignature((String) responseBody.get("signature"));
                responseModel.setQrCodeUrl((String) responseBody.get("qrCodeUrl"));
                responseModel.setDeeplink((String) responseBody.get("deeplink"));
                responseModel.setDeeplinkWebInApp((String) responseBody.get("deeplinkWebInApp"));

                return responseModel;
            } else {
                logger.error("Failed to get response from Momo");
                throw new RuntimeException("Failed to get response from Momo");
            }
        } catch (Exception e) {
            logger.error("Error creating Momo payment request", e);
            throw new RuntimeException("Error creating Momo payment request: " + e.getMessage(), e);
        }
    }

    public MomoExecuteResponseModel processPaymentResponse(Map<String, String> response) {
        logger.info("Processing Momo payment response: {}", response);

        try {
            // Validate the response
            boolean isValidResponse = verifyPaymentResponse(response);

            if (!isValidResponse) {
                logger.error("Invalid payment response signature from Momo");
                return null;
            }

            // Extract response parameters
            String orderId = response.get("orderId");
            String amount = response.get("amount");
            String orderInfo = response.get("orderInfo");
            String transId = response.get("transId");
            String responseTime = response.get("responseTime");
            String resultCode = response.get("resultCode");
            String payType = response.get("payType");

            // Create and return the response model
            return MomoExecuteResponseModel.builder()
                    .orderId(orderId)
                    .amount(amount)
                    .orderInfo(orderInfo)
                    .transId(transId)
                    .responseTime(responseTime)
                    .resultCode(resultCode)
                    .payType(payType)
                    .build();

        } catch (Exception e) {
            logger.error("Error processing Momo payment response", e);
            return null;
        }
    }

    public boolean verifyPaymentResponse(Map<String, String> response) {
        try {
            // Extract the signature from the response
            String responseSignature = response.get("signature");
            if (responseSignature == null) {
                logger.error("No signature found in Momo payment response");
                return false;
            }

            // Build the raw signature string for verification
            // The parameters must be in the same order as they were when creating the signature
            StringBuilder rawSignature = new StringBuilder();
            rawSignature.append("accessKey=").append(accessKey);

            // Add all parameters to the signature except the signature itself
            for (Map.Entry<String, String> entry : response.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (!"signature".equals(key) && value != null) {
                    rawSignature.append("&").append(key).append("=").append(value);
                }
            }

            // Calculate the expected signature
            String expectedSignature = signHmacSHA256(rawSignature.toString(), secretKey);

            // Compare the signatures
            boolean isValid = expectedSignature.equals(responseSignature);

            if (!isValid) {
                logger.warn("Momo payment response signature verification failed. Expected: {}, Actual: {}",
                        expectedSignature, responseSignature);
            }

            return isValid;
        } catch (Exception e) {
            logger.error("Error verifying Momo payment response", e);
            return false;
        }
    }

    private String signHmacSHA256(String data, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
