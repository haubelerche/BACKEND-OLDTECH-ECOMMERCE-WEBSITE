package com.example.BACKEND_OLDTECH_WEBSITE.Model;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AddressTypeEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.sql.Timestamp;
@Entity
@Table(name = "address")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;

    @Column(name = "city", length= 100)
    @NotBlank(message = "Thông tin không được để trống")
    private String city;

    @Column(name = "district", length =100)
    @NotBlank(message = "Thông tin không được để trống")
    private String district;

    @Column(name = "ward", length = 100)
    @NotBlank(message = "Thông tin không được để trống")
    private String ward;

    @Column(name = "street", length = 200)
    @NotBlank(message = "Thông tin không được để trống")
    private String street;

    @Column(name = "address_type")   //home, cpn, warehouse, other
    private AddressTypeEnum addressType;
    @Column(name = "is_default", columnDefinition = "TINYINT(1)")
    private Boolean isDefault;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}