package com.home.service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianServicePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Services service;

    private Double price;

    public TechnicianServicePrice(Technician technician, Services service, Double price) {

        this.technician = technician;

        this.service = service;

        this.price = price;

    }
}
