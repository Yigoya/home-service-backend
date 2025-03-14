package com.home.service.models;

import com.home.service.models.enums.CompanySize;
import com.home.service.models.enums.CustomerStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "business_customers")
@Getter
@Setter
public class BusinessCustomer extends BaseEntity {

    @NotBlank
    private String name;

    @Email
    private String email;

    private String contactPerson;

    private String phone;

    private String industry;

    @Enumerated(EnumType.STRING)
    private CompanySize size;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status;

    private String lastOrderDate;

    private double totalSpent;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Business company;

    // Getters and Setters
}
