package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressListResponse {
    private List<AddressRequest> addresses;
}
