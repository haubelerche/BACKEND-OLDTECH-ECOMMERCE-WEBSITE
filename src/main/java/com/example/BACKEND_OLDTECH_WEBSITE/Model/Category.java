package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "name", length = 100)
    @NotBlank(message = "Thông tin không được để trống")
    private String name;

    @Column(name = "description", columnDefinition = "MEDIUMTEXT")
    @NotBlank(message = "Thông tin không được để trống")
    private String description;

    @Column(name = "avatar_url", length = 2048)
    @NotBlank(message = "Thông tin không được để trống")
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category Category;

    @OneToMany(mappedBy = "parentCategory")
    private List<Category> subcategories;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}