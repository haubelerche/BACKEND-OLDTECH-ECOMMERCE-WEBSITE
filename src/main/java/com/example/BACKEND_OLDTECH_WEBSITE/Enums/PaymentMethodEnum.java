package com.example.BACKEND_OLDTECH_WEBSITE.Enums;

/**
 * Enumeration for payment methods.
 * The order matters to match with database constraint:
 * CashOnDelivery (0), Momo (1)
 */
public enum PaymentMethodEnum {
    CashOnDelivery,  // Value 0 in database
    Momo            // Value 1 in database
}
