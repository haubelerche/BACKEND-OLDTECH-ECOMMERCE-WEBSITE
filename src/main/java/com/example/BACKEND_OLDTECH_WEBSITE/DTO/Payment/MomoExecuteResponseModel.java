package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoExecuteResponseModel {
    private String orderId;
    private String amount;
    private String orderInfo;
    private String transId;
    private String responseTime;
    private String resultCode;
    private String payType;
}
