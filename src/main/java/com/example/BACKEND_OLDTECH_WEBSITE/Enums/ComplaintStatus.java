package com.example.BACKEND_OLDTECH_WEBSITE.Enums;

public enum ComplaintStatus {
    Pending("Chờ xử lý"),
    Reviewing("Đang xem xét"),
    InProgress("Đang xử lý"),
    Resolved("Đã giải quyết"),
    Rejected("Từ chối"),
    Closed("Đã đóng");
    
    private final String displayName;
    
    ComplaintStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static String getValidStatusesString() {
        StringBuilder sb = new StringBuilder();
        for (ComplaintStatus status : values()) {
            sb.append(status.name()).append(" (").append(status.displayName).append("), ");
        }
        return sb.toString().replaceAll(", $", "");
    }
}