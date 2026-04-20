package com.home.service.dto;

import com.home.service.models.Technician;
import com.home.service.models.TechnicianAddress;
import com.home.service.models.enums.EthiopianLanguage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicTechnicianDTO {
    private Long id;
    private String name;
    private String businessName;
    private Integer yearsExperience;
    private String serviceArea;
    private String bio;
    private String availability;
    private Double rating;
    private Integer completedJobs;
    private Boolean verified;
    private String city;
    private String subcity;
    private String wereda;
    private String country;
    private Double latitude;
    private Double longitude;
    private String profileImage;
    private Set<ServiceDTO> services;

    public PublicTechnicianDTO(Technician technician, EthiopianLanguage language) {
        this.id = technician.getId();
        this.name = technician.getUser() != null ? technician.getUser().getName() : null;
        this.businessName = technician.getBusinessName();
        this.yearsExperience = technician.getYearsExperience();
        this.serviceArea = technician.getServiceArea();
        this.bio = technician.getBio();
        this.availability = technician.getAvailability();
        this.rating = technician.getRating();
        this.completedJobs = technician.getCompletedJobs();
        this.verified = technician.getVerified();
        this.profileImage = technician.getUser() != null ? technician.getUser().getProfileImage() : null;

        this.services = technician.getServices() == null
                ? Set.of()
                : technician.getServices().stream()
                        .map(service -> new ServiceDTO(service, language))
                        .collect(Collectors.toSet());

        if (technician.getTechnicianAddresses() != null && !technician.getTechnicianAddresses().isEmpty()) {
            TechnicianAddress address = technician.getTechnicianAddresses().get(0);
            this.city = address.getCity();
            this.subcity = address.getSubcity();
            this.wereda = address.getWereda();
            this.country = address.getCountry();
            this.latitude = address.getLatitude();
            this.longitude = address.getLongitude();
        }
    }
}
