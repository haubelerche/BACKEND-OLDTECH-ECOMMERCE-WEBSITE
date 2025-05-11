package com.example.BACKEND_OLDTECH_WEBSITE.Enums;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum OrderStatusEnum {
    Pending, Processing, Shipped, Delivered, Cancelled, Returned, Completed;

    public static String[] names() {
        return Arrays.stream(values()).map(OrderStatusEnum::name).toArray(String[]::new);
    }

    public static String getValidStatusesString() {
        return Arrays.stream(names()).collect(Collectors.joining(", "));
    }
}
