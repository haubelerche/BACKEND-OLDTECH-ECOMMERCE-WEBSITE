package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment;

import lombok.Data;

@Data
public class MomoCreatePaymentResponseModel {
    private String requestId;
    private int errorCode;
    private String orderId;
    private String message;
    private String localMessage;
    private String requestType;
    private String payUrl;
    private String signature;
    private String qrCodeUrl;
    private String deeplink;
    private String deeplinkWebInApp;
}
