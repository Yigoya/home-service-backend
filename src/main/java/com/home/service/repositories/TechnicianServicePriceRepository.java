package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Services;
import com.home.service.models.Technician;
import com.home.service.models.TechnicianServicePrice;

import java.util.Optional;

public interface TechnicianServicePriceRepository extends JpaRepository<TechnicianServicePrice, Long> {
    Optional<TechnicianServicePrice> findByTechnicianAndService(Technician technician, Services service);
}
