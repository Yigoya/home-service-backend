package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.TechnicianWeeklySchedule;

public interface TechnicianWeeklyScheduleRepository extends JpaRepository<TechnicianWeeklySchedule, Long> {

    // Find the weekly schedule for a specific technician
    TechnicianWeeklySchedule findByTechnicianId(Long technicianId);
}
