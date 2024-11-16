package com.home.service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.TechnicianAddress;

public interface TechnicianAddressRepository extends JpaRepository<TechnicianAddress, Long> {

    Optional<TechnicianAddress> findByTechnicianId(Long technicianId);
}
