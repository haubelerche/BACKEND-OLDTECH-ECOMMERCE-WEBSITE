package com.example.BACKEND_OLDTECH_WEBSITE.Model;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AddressTypeEnum;
import jakarta.persistence.*;
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
    @Column(name = "address_id", columnDefinition = "INT UNSIGNED")
    private Integer addressId;

    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;
}