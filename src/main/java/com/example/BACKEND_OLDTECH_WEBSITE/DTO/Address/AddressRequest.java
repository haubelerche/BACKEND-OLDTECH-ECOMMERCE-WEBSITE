package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AddressTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "Thành phố không được để trống")
    private String city;

    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;

    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;

    @NotBlank(message = "Đường không được để trống")
    private String street;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String detailedAddress;

    @NotNull(message = "Loại địa chỉ không được để trống")
    private AddressTypeEnum addressType;

    private Boolean isDefault = false;

    @Override
    public String toString() {
        return "AddressRequest{" +
                "city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", ward='" + ward + '\'' +
                ", street='" + street + '\'' +
                ", detailedAddress='" + detailedAddress + '\'' +
                ", addressType=" + addressType +
                ", isDefault=" + isDefault +
                '}';
    }
}
