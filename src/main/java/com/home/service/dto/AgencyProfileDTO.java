package com.home.service.dto;

import com.home.service.models.AgencyProfile;
import com.home.service.models.enums.VerificationStatus;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AgencyProfileDTO {

    private Long id;
    private String businessName;
    private String description;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;
    private String website;
    private String document;
    private String image;
    private VerificationStatus verificationStatus;
    private List<ServiceDTO> services;

    // Constructor, getters, and setters

    public AgencyProfileDTO(AgencyProfile agencyProfile) {
        this.id = agencyProfile.getId();
        this.businessName = agencyProfile.getBusinessName();
        this.description = agencyProfile.getDescription();
        this.address = agencyProfile.getAddress();
        this.city = agencyProfile.getCity();
        this.state = agencyProfile.getState();
        this.zip = agencyProfile.getZip();
        this.country = agencyProfile.getCountry();
        this.phone = agencyProfile.getPhone();
        this.website = agencyProfile.getWebsite();
        this.document = agencyProfile.getDocument();
        this.image = agencyProfile.getImage();
        this.verificationStatus = agencyProfile.getVerificationStatus();
    }

    // Getters and setters for services
    public List<ServiceDTO> getServices() {
        return services;
    }

    public void setServices(List<ServiceDTO> services) {
        this.services = services;
    }
}