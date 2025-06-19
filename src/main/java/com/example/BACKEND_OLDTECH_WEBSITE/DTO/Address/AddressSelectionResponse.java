package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AddressTypeEnum;
import lombok.Data;

@Data
public class AddressSelectionResponse {
    private Integer addressId;
    private String displayName; // Formatted address for display
    private String fullAddress; // Complete address string
    private Boolean isDefault;
    private AddressTypeEnum addressType; // HOME, WORK, etc.
    
    // Individual components for frontend if needed
    private String city;
    private String district;
    private String ward;
    private String street;
    private String detailedAddress;
    
    // Selection metadata
    private boolean canBeUsedForOrders;
    private String selectionLabel; // User-friendly label for selection dropdown
}
