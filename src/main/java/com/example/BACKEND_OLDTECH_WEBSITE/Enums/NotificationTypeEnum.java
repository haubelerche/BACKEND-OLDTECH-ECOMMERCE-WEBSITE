package com.example.BACKEND_OLDTECH_WEBSITE.Enums;

public enum NotificationTypeEnum {
    // System notifications
    SYSTEM("Hệ thống"),
    ACCOUNT_SETUP("Thiết lập tài khoản"),
    PROFILE_INCOMPLETE("Thông tin chưa đầy đủ"),

    // Admin notifications
    ADMIN_MESSAGE("Thông báo từ Admin"),
    ADMIN_ANNOUNCEMENT("Thông báo chung"),
    ACCOUNT_VERIFICATION("Xác minh tài khoản"),

    // Order related
    ORDER_UPDATE("Cập nhật đơn hàng"),
    ORDER_SHIPPED("Đơn hàng đã giao"),
    ORDER_DELIVERED("Đơn hàng đã nhận"),
    ORDER_CANCELLED("Đơn hàng đã hủy"),

    // Product & Shopping
    NEW_PRODUCT("Sản phẩm mới"),
    PROMOTION("Khuyến mãi"),
    PRICE_DROP("Giảm giá"),
    BACK_IN_STOCK("Hàng có sẵn trở lại"),

    // Reviews & Feedback
    REVIEW_UPDATE("Cập nhật đánh giá"),
    REVIEW_REPLY("Phản hồi đánh giá"),
    
    // Support & Complaints
    COMPLAINT_UPDATE("Cập nhật khiếu nại"),
    SUPPORT_REPLY("Phản hồi hỗ trợ"),

    // Financial
    REFUND_UPDATE("Cập nhật hoàn tiền"),
    PAYMENT_SUCCESS("Thanh toán thành công"),
    PAYMENT_FAILED("Thanh toán thất bại"),

    // General
    OTHER("Khác");
    
    private final String displayName;
    
    NotificationTypeEnum(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

