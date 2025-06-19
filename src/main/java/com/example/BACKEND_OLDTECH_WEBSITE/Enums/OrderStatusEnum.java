package com.example.BACKEND_OLDTECH_WEBSITE.Enums;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum OrderStatusEnum {
    Pending,
    Processing,
    Shipped,
    Delivered,
    Completed,
    Cancelled,
    Returned;

    public static String getValidStatusesString() {
        return Arrays.stream(OrderStatusEnum.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));
    }
}
