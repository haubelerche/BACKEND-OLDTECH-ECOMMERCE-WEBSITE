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
    @Column(name = "address_id", nullable = false)
    private Integer addressId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(name = "city", length= 100, nullable = false)
   
    private String city;

    @Column(name = "district", length =100, nullable = false)
   
    private String district;

    @Column(name = "ward", length = 100, nullable = false)
   
    private String ward;

    @Column(name = "street", length = 200, nullable = false)
   
    private String street;

    @Column(name = "detailed_address", nullable = false)
    private String detailedAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)   //home, cpn, warehouse, other
    private AddressTypeEnum addressType;

    @Column(name = "is_default", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDefault;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
}