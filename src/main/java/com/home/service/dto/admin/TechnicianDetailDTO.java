package com.home.service.dto.admin;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.home.service.dto.ServiceDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianDetailDTO {
    private Long technicianId;
    private String name;
    private String email;
    private String phoneNumber;
    private String profileImage;
    private String bio;
    private Double rating;
    private Integer completedJobs;
    private Set<ServiceDTO> services;
    private TechnicianAddressDTO address;
}
