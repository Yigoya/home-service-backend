package com.home.service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Technician extends BaseEntity {
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Lob
    @Column(length = 512)
    private String bio;
    private String availability = "Available";
    private Double rating;
    private Integer completedJobs;

    @ElementCollection
    private List<String> documents;

    private String idCardImage;

    @OneToOne(mappedBy = "technician", cascade = CascadeType.ALL, orphanRemoval = true)
    private TechnicianWeeklySchedule weeklySchedule;

    @OneToMany(mappedBy = "technician", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TechnicianServicePrice> servicePrices;

    @ManyToMany
    @JoinTable(name = "technician_services", joinColumns = @JoinColumn(name = "technician_id"), inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<Services> services;

    private Boolean verified = false;

    @OneToMany(mappedBy = "technician", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TechnicianAddress> technicianAddresses = new ArrayList<>();
}
