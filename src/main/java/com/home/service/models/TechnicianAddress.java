package com.home.service.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianAddress extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;
    private String street;
    private String city;
    private String subcity;
    private String wereda;
    private String country;
    private String zipCode;
    private Double latitude;
    private Double longitude;
}
