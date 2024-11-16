package com.home.service.dto.records;

import com.home.service.models.Services;
import com.home.service.models.TechnicianWeeklySchedule;
import com.home.service.dto.ReviewDTO;
import com.home.service.dto.TechnicianWeeklyScheduleDTO;
import com.home.service.models.Review;

import java.util.List;
import java.util.Set;

public record SingleTechnician(Long id, String name, String email, String city, String phoneNumber, String profileImage,

                String bio, String role, Set<Services> services, Double rating, Integer bookings,

                TechnicianWeeklyScheduleDTO schedule, List<ReviewDTO> review) {

}
