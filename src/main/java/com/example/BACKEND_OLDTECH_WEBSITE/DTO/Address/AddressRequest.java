package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Data
public class AddressRequest {
    @NotBlank(message = "Tên thành phố không được để trống")
    @Size(min = 3, max = 100, message = "Tên thành phố phải có độ dài từ 3 đến 100 ký tự")
    private String city;

    @NotBlank(message = "Tên quận/huyện không được để trống")
    @Size(min = 3, max = 100, message = "Tên quận/huyện phải có độ dài từ 3 đến 100 ký tự")
    private String district;

    @NotBlank(message = "Tên phường/xã không được để trống")
    @Size(min = 3, max = 100, message = "Tên phường/xã phải có độ dài từ 3 đến 100 ký tự")
    private String ward;

    @NotBlank(message = "Tên đường không được để trống")
    @Size(min = 3, max = 255, message = "Tên đường phải có độ dài từ 3 đến 255 ký tự")
    private String street;
    @NotBlank(message = "Tên đia chỉ chi tiết không được để trống")
    private String detailedAddress;

    @NotBlank(message = "Loại địa chỉ không được để trống")
    private String addressType;
    private Boolean isDefault;
    private List<AddressResponse> addresses;
}
