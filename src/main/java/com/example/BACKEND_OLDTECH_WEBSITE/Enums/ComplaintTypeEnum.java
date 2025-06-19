package com.example.BACKEND_OLDTECH_WEBSITE.Enums;

public enum ComplaintTypeEnum {
    // Product related complaints
    PRODUCT_QUALITY("Chất lượng sản phẩm"),
    PRODUCT_DESCRIPTION("Mô tả sản phẩm không đúng"),
    PRODUCT_COUNTERFEIT("Sản phẩm giả mạo"),
    PRODUCT_PRICING("Vấn đề về giá cả"),
    
    // Order related complaints
    ORDER_DELIVERY("Vấn đề giao hàng"),
    ORDER_DELAY("Chậm trễ đơn hàng"),
    ORDER_CANCELLATION("Hủy đơn hàng"),
    ORDER_BILLING("Vấn đề thanh toán"),
    
    // Seller related complaints
    SELLER_BEHAVIOR("Hành vi người bán"),
    SELLER_COMMUNICATION("Dịch vụ tư vấn của người bán"),
    SELLER_FRAUD("Người bán lừa đảo"),
    SELLER_SERVICE("Dịch vụ của người bán"),
    
    // Platform related complaints
    PLATFORM_TECHNICAL("Lỗi kỹ thuật"),
    PLATFORM_SECURITY("Vấn đề bảo mật"),
    PLATFORM_POLICY("Chính sách nền tảng"),
    PLATFORM_ACCOUNT("Vấn đề tài khoản"),
    
    // User behavior complaints
    USER_HARASSMENT("Quấy rối"),
    USER_SPAM("Spam"),
    USER_INAPPROPRIATE_CONTENT("Nội dung không phù hợp"),
    USER_FAKE_REVIEW("Đánh giá giả mạo"),
    
    // General complaints
    OTHER("Khác");
    
    private final String displayName;
    
    ComplaintTypeEnum(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static String getAllTypesString() {
        StringBuilder sb = new StringBuilder();
        for (ComplaintTypeEnum type : values()) {
            sb.append(type.name()).append(" (").append(type.displayName).append("), ");
        }
        return sb.toString().replaceAll(", $", "");
    }
}
