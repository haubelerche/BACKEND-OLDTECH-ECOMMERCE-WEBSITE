package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Integer cartItemId;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private ShoppingCart cartId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "added_at")
    private Timestamp addedAt;
}