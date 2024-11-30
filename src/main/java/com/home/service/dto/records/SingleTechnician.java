package com.home.service.dto.records;

import com.home.service.dto.ReviewDTO;
import com.home.service.dto.ServiceDTO;
import com.home.service.dto.TechnicianWeeklyScheduleDTO;

import java.util.List;
import java.util.Set;

public record SingleTechnician(Long id, String name, String email, String city, String subcity, String phoneNumber,
        String profileImage,

        String bio, String role, Set<ServiceDTO> services, Double rating, Integer bookings,

        TechnicianWeeklyScheduleDTO schedule, List<ReviewDTO> review) {

}
