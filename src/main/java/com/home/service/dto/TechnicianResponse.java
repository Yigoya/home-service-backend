package com.home.service.dto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.home.service.models.Technician;
import com.home.service.models.enums.EthiopianLanguage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianResponse {
    private Long id;
    private String bio;
    private Double rating;
    private Integer completedJobs;
    private String idCardImage;
    private List<String> documents;
    private Set<ServiceDTO> services;

    public TechnicianResponse(Technician technician, EthiopianLanguage language) {

        this.id = technician.getId();
        this.bio = technician.getBio();
        this.rating = technician.getRating();
        this.completedJobs = technician.getCompletedJobs();
        this.idCardImage = technician.getIdCardImage();
        this.documents = technician.getDocuments();
        this.services = technician.getServices().stream()
                .map(service -> new ServiceDTO(service, language))
                .collect(Collectors.toSet());
    }
}
