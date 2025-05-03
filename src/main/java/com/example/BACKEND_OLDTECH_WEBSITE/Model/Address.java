package com.example.BACKEND_OLDTECH_WEBSITE.Model;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AddressTypeEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.sql.Timestamp;
@Entity
@Table(name = "address")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer addressId;


    private Integer userId;


    private String city;


    private String district;


    private String ward;


    private String street;

    private String detailedAddress;

    private AddressTypeEnum addressType;

    private Boolean isDefault;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}