package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AddressTypeEnum;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "address")
public class Address {    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;

    @Column(name="user_id", nullable = false)
    private Integer userId;

    @Column(name="city", nullable = false)
    private String city;

    @Column(name="district", nullable = false)
    private String district;

    @Column(name="ward", nullable = false)
    private String ward;

    @Column(name="street", nullable = false)
    private String street;

    @Column(name="detailed_address", nullable = false)
    private String detailedAddress;    @Enumerated(EnumType.STRING)
    @Column(name = "address_type")
    private AddressTypeEnum addressType;

    @Column(name="is_default", nullable = false)
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
