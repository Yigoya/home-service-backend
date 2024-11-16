package com.home.service.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    private String street;
    private String city;
    private String subcity;
    private String wereda;
    private String state;
    private String country;
    private String zipCode;
    private Double latitude;
    private Double longitude;

}
