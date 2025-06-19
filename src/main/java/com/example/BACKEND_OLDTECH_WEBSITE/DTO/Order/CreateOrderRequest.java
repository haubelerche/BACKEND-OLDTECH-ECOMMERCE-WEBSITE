package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Order;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMax;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CreateOrderRequest {
    private static final Logger logger = LoggerFactory.getLogger(CreateOrderRequest.class);

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethodEnum paymentMethod;
    
    @NotNull(message = "Tổng tiền không được để trống")
    @Positive(message = "Tổng tiền phải lớn hơn 0")
    @DecimalMax(value = "1000000000", message = "Tổng tiền quá lớn, không thể vượt quá 1 tỷ")
    private BigDecimal totalAmount;
    
    private OrderStatusEnum status; // Optional, will default to PENDING if not provided
    
    private String notes; // Optional
    
    @Min(value = 1, message = "ID địa chỉ giao hàng phải lớn hơn 0")
    private Integer shippingAddressId; // Optional, will use user's first address if not provided

    /**
     * Custom setter for totalAmount to handle different input types
     * This allows the API to accept both numeric values and strings
     */
    @JsonSetter("totalAmount")
    public void setTotalAmount(Object value) {
        try {
            if (value == null) {
                this.totalAmount = null;
            } else if (value instanceof Number) {
                // Handle Number types (Integer, Double, etc.)
                this.totalAmount = new BigDecimal(value.toString());
                validateTotalAmount();
            } else if (value instanceof String) {
                // Handle String representation of numbers
                String strValue = ((String) value).trim();
                if (!strValue.isEmpty()) {
                    this.totalAmount = new BigDecimal(strValue);
                    validateTotalAmount();
                } else {
                    this.totalAmount = null;
                }
            } else if (value instanceof Map) {
                // Handle JSON objects that might contain value information
                Object mapValue = ((Map<?, ?>) value).get("value");
                if (mapValue != null) {
                    setTotalAmount(mapValue); // Recursive call to handle the value
                } else {
                    this.totalAmount = null;
                }
            } else {
                logger.warn("Unexpected type for totalAmount: {}", value.getClass().getName());
                this.totalAmount = null;
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid number format for totalAmount: {}", value, e);
            throw new IllegalArgumentException("Tổng tiền không hợp lệ: " + value);
        } catch (Exception e) {
            logger.error("Error setting totalAmount: {}", value, e);
            throw new IllegalArgumentException("Lỗi xử lý tổng tiền: " + e.getMessage());
        }
    }

    /**
     * Validates that the total amount meets database constraints
     */
    private void validateTotalAmount() {
        if (this.totalAmount != null) {
            // Ensure the amount is positive
            if (this.totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Tổng tiền phải lớn hơn 0");
            }

            // Check upper limit to prevent database constraint violation
            if (this.totalAmount.compareTo(new BigDecimal("1000000000")) > 0) {
                throw new IllegalArgumentException("Tổng tiền quá lớn, không thể vượt quá 1 tỷ");
            }
        }
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "paymentMethod=" + paymentMethod +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", notes='" + notes + '\'' +
                ", shippingAddressId=" + shippingAddressId +
                '}';
    }
}
