package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.Technician;
import com.home.service.models.User;
import com.home.service.models.Services;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TechnicianRepository extends JpaRepository<Technician, Long>, JpaSpecificationExecutor<Technician> {
        Optional<Technician> findByUser(User user);

        List<Technician> findByVerifiedFalse();

        Optional<Technician> findByIdAndVerifiedFalse(Long id);

        List<Technician> findByServices(Services service);

        @Query("SELECT t FROM Technician t JOIN t.services s WHERE s.id = :serviceId")
        List<Technician> findTechniciansByServiceId(@Param("serviceId") Long serviceId);

        @Query("SELECT t FROM Technician t JOIN t.user u WHERE " +
                        "(:query IS NULL OR u.name LIKE %:query%) ")
        List<Technician> findTechnicians(@Param("query") String query);

        @Query("SELECT t FROM Technician t LEFT JOIN TechnicianWeeklySchedule tws ON t.id = tws.technician.id  " +
                        "JOIN t.services s " +
                        "WHERE s.id = :serviceId AND t.availability = 'Available' " +
                        "AND (tws IS NULL OR " +
                        "(:dayOfWeek = 'MONDAY' AND :time BETWEEN tws.mondayStart AND tws.mondayEnd " +
                        "OR :dayOfWeek = 'TUESDAY' AND :time BETWEEN tws.tuesdayStart AND tws.tuesdayEnd " +
                        "OR :dayOfWeek = 'WEDNESDAY' AND :time BETWEEN tws.wednesdayStart AND tws.wednesdayEnd " +
                        "OR :dayOfWeek = 'THURSDAY' AND :time BETWEEN tws.thursdayStart AND tws.thursdayEnd " +
                        "OR :dayOfWeek = 'FRIDAY' AND :time BETWEEN tws.fridayStart AND tws.fridayEnd " +
                        "OR :dayOfWeek = 'SATURDAY' AND :time BETWEEN tws.saturdayStart AND tws.saturdayEnd " +
                        "OR :dayOfWeek = 'SUNDAY' AND :time BETWEEN tws.sundayStart AND tws.sundayEnd))")
        Page<Technician> findAvailableTechniciansByServiceAndSchedule(
                        @Param("serviceId") Long serviceId,
                        @Param("dayOfWeek") String dayOfWeek,
                        @Param("time") LocalTime time,
                        Pageable pageable);
}
