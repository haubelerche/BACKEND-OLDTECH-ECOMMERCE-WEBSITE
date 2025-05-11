package com.example.BACKEND_OLDTECH_WEBSITE.Enums;

public enum ProductStatusEnum {
    Pending, Approved, Sold, Hidden, Rejected;
}
//Sold => automatically set to hidden
//Rejected => automatically set to hidden
//Approved => automatically set to available
//Pending => automatically set to available
//Hidden => automatically set to available


