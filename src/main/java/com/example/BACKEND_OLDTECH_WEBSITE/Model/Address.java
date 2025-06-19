package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AddressTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "address")
public class Address {
      @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", columnDefinition = "INT UNSIGNED")
    private Integer addressId;

    @Column(name="user_id", nullable = false, columnDefinition = "INT UNSIGNED")
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
    private String detailedAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type")
    private AddressTypeEnum addressType;

    @Builder.Default
    @Column(name="is_default", nullable = false)
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
