package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.home.service.models.Technician;
import com.home.service.models.TechnicianWeeklySchedule;
import com.home.service.models.enums.EthiopianLanguage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String businessName;
    private Integer yearsExperience;
    private String serviceArea;
    private String bio;
    private String profileImage;
    private String idCardImage;
    private List<String> documents;
    private List<String> licenses;
    private List<ServiceDTO> services;
    private Double rating;
    private Integer completedJobs;
    private TechnicianWeeklyScheduleDTO weeklySchedule;
    private List<Map<String, Object>> calender;
    private List<AddressDTO> address;
    // Social media and website
    private String website;
    private String facebook;
    private String twitter;
    private String instagram;
    private String linkedin;
    private String whatsapp;

    public TechnicianProfileDTO(Long id, String name, String email, String bio, Double rating, Integer completedJobs,
            TechnicianWeeklySchedule weeklySchedule) {

        this.id = id;

        this.name = name;

        this.email = email;

        this.bio = bio;

        this.rating = rating;

        this.completedJobs = completedJobs;

        this.weeklySchedule = weeklySchedule != null ? new TechnicianWeeklyScheduleDTO(weeklySchedule) : null;

    }

    public TechnicianProfileDTO(Technician technician, EthiopianLanguage language) {

        this.id = technician.getId();

        this.name = technician.getUser().getName();

        this.email = technician.getUser().getEmail();

        this.profileImage = technician.getUser().getProfileImage();

        this.phoneNumber = technician.getUser().getPhoneNumber();

        this.bio = technician.getBio();
    this.businessName = technician.getBusinessName();
    this.yearsExperience = technician.getYearsExperience();
    this.serviceArea = technician.getServiceArea();

        this.idCardImage = technician.getIdCardImage();
        this.documents = technician.getDocuments();
    this.licenses = technician.getLicenses();

        this.services = technician.getServices().stream().map(service -> new ServiceDTO(service, language))
                .collect(Collectors.toList());

        this.rating = technician.getRating();

        this.completedJobs = technician.getCompletedJobs();

        // Social
        this.website = technician.getWebsite();
        this.facebook = technician.getFacebook();
        this.twitter = technician.getTwitter();
        this.instagram = technician.getInstagram();
        this.linkedin = technician.getLinkedin();
        this.whatsapp = technician.getWhatsapp();
    }
}
