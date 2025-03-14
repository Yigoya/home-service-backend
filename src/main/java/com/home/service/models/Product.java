package com.home.service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product extends BaseEntity {

    @NotBlank
    private String name;

    @Column(length = 5000)
    private String description;

    private double price;

    private String category;

    private String image;

    private boolean inStock;

    private String sku;

    private int inventory;

    private int minimumOrderQuantity;

    private int leadTime;

    private String unitOfMeasure;

    @ElementCollection
    private Map<String, String> specifications;

    @ElementCollection
    private List<String> certifications;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Business company;

    // Getters and Setters
}