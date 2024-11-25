package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.home.service.models.Services;
import com.home.service.models.Technician;
import com.home.service.models.TechnicianWeeklySchedule;
import com.home.service.models.enums.EthiopianLanguage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String bio;
    private String idCardImage;
    private List<ServiceDTO> services;
    private Double rating;
    private Integer completedJobs;
    private TechnicianWeeklyScheduleDTO weeklySchedule;
    private List<Map<String, Object>> calender;

    public TechnicianProfileDTO(Long id, String name, String email, String bio, Double rating, Integer completedJobs,
            TechnicianWeeklySchedule weeklySchedule) {

        this.id = id;

        this.name = name;

        this.email = email;

        this.bio = bio;

        this.rating = rating;

        this.completedJobs = completedJobs;

        this.weeklySchedule = new TechnicianWeeklyScheduleDTO(weeklySchedule);

    }

    public TechnicianProfileDTO(Technician technician, EthiopianLanguage language) {

        this.id = technician.getId();

        this.name = technician.getUser().getName();

        this.email = technician.getUser().getEmail();

        this.phoneNumber = technician.getUser().getPhoneNumber();

        this.bio = technician.getBio();

        this.idCardImage = technician.getIdCardImage();

        this.services = technician.getServices().stream().map(service -> new ServiceDTO(service, language))
                .collect(Collectors.toList());

        this.rating = technician.getRating();

        this.completedJobs = technician.getCompletedJobs();
    }
}
