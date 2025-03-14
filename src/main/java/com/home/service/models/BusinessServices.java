package com.home.service.models;

import java.util.ArrayList;
import java.util.List;

import com.home.service.models.enums.ServiceLevel;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "business_services")
@Getter
@Setter
public class BusinessServices extends BaseEntity {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    @Column(length = 5000)
    private String description;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    private String tag;

    private String image;

    private boolean available;

    private int duration;

    private int deliveryTime;

    @Enumerated(EnumType.STRING)
    private ServiceLevel serviceLevel;

    @ElementCollection
    private List<String> requirements;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOption> options = new ArrayList<>();

    public void addOption(ServiceOption option) {
        options.add(option);
        option.setService(this);
    }

    public void removeOption(ServiceOption option) {
        options.remove(option);
        option.setService(null);
    }

    @Entity
    @Table(name = "service_options")
    @Getter
    @Setter
    public static class ServiceOption extends BaseEntity {
        private String name;
        private String description;

        @ManyToOne
        @JoinColumn(name = "service_id")
        private BusinessServices service;

        @ElementCollection
        @CollectionTable(name = "service_option_choices")
        private List<String> choices;

        @ElementCollection
        @CollectionTable(name = "service_option_prices")
        private List<Double> prices;
    }

    // Getters and Setters
}