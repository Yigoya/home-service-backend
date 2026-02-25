package com.home.service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.firebase.database.annotations.NotNull;
import com.home.service.models.enums.ProductCondition;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product extends BaseEntity {

    @NotBlank
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    private Double price;

    private String currency = "USD";

    private Integer stockQuantity;

    private Integer minOrderQuantity;

    @ElementCollection
    private List<String> images;

    private String category;

    private String sku;

    private boolean isActive = true;

    @NotNull
    private Boolean inStock = true; // Added field

    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition")
    private ProductCondition condition = ProductCondition.NEW;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(columnDefinition = "TEXT")
    private String specifications;

    @ManyToMany
    @JoinTable(name = "product_services",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<Services> services = new HashSet<>();
}