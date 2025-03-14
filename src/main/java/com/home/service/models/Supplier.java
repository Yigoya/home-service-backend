package com.home.service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.home.service.models.enums.SupplierStatus;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
public class Supplier extends BaseEntity {

    @NotBlank
    private String name;

    private String contactPerson;

    @Email
    private String email;

    private String phone;

    private String address;

    private String city;

    private String state;

    private String zip;

    private String country;

    private String category;

    @Enumerated(EnumType.STRING)
    private SupplierStatus status;

    private double rating;

    private String paymentTerms;

    private int leadTime;

    private double minimumOrderValue;

    @ElementCollection
    private List<String> products;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Business company;

    // Getters and Setters
}
