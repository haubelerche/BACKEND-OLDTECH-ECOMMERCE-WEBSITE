package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "category")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer category_id;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    @Column(unique = true)
    private String slug;

    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Category> subcategories = new HashSet<>();

    @Column(name = "is_visible", columnDefinition = "TINYINT(1)")
    private boolean isVisible = true;
}