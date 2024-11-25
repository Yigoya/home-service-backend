package com.home.service.dto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.home.service.models.Technician;
import com.home.service.models.enums.EthiopianLanguage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianDTO {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
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
    private List<String> documents;
    private String profileImage;
    private Set<ServiceDTO> services;

    public TechnicianDTO(Technician technician, EthiopianLanguage language) {
        this.id = technician.getId();
        this.name = technician.getUser().getName();
        this.email = technician.getUser().getEmail();
        this.phoneNumber = technician.getUser().getPhoneNumber();
        this.bio = technician.getBio();
        this.availability = technician.getAvailability();
        this.rating = technician.getRating();
        this.completedJobs = technician.getCompletedJobs();
        this.verified = technician.getVerified();

        this.documents = technician.getDocuments();
        this.profileImage = technician.getUser().getProfileImage();
        this.services = technician.getServices().stream()
                .map(service -> new ServiceDTO(service, language))
                .collect(Collectors.toSet());
    }
    // Getters and Setters
}
