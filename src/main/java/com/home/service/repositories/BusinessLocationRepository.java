package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.home.service.models.BusinessLocation;
import com.home.service.models.enums.LocationType;

public interface BusinessLocationRepository extends JpaRepository<BusinessLocation, Long> {
    Page<BusinessLocation> findByType(LocationType type, Pageable pageable);

    @Query("SELECT bl FROM BusinessLocation bl WHERE " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(bl.coordinates.latitude)) * " +
            "cos(radians(bl.coordinates.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
            "sin(radians(bl.coordinates.latitude)))) <= :radius")
    Page<BusinessLocation> findNearby(double latitude, double longitude, double radius, Pageable pageable);
}