package com.home.service.models;

import com.home.service.Service.BusinessLocationService;
import com.home.service.Service.BusinessLocationService.BusinessLocationDTO;
import com.home.service.models.enums.Coordinates;
import com.home.service.models.enums.LocationType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "business_locations")
@Getter
@Setter
public class BusinessLocation extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private LocationType type;

    @OneToOne(mappedBy = "location")
    private Business business;

    @ManyToOne
    @JoinColumn(name = "parent_location_id", nullable = true)
    private BusinessLocation parentLocation;

    @Embedded
    private Coordinates coordinates;

    private String street;

    private String kebele;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    // existing fields and methods

    public BusinessLocation() {
    }

    public BusinessLocation(BusinessLocationDTO dto) {
        this.name = dto.name;
        this.type = dto.type;
        this.coordinates = dto.coordinates;
        this.street = dto.street;
        this.kebele = dto.kebele;
        this.city = dto.city;
        this.state = dto.state;
        this.postalCode = dto.postalCode;
        this.country = dto.country;

    }

    // Getters and Setters
}
