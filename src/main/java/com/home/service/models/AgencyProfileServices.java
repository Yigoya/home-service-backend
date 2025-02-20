package com.home.service.models;

import jakarta.persistence.*;

@Entity
@Table(name = "agency_profile_services")
public class AgencyProfileServices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agency_profile_id", nullable = false)
    private AgencyProfile agencyProfile;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Services service;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AgencyProfile getAgencyProfile() {
        return agencyProfile;
    }

    public void setAgencyProfile(AgencyProfile agencyProfile) {
        this.agencyProfile = agencyProfile;
    }

    public Services getService() {
        return service;
    }

    public void setService(Services service) {
        this.service = service;
    }
}