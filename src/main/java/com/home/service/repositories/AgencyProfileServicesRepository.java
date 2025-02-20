package com.home.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.AgencyProfile;
import com.home.service.models.AgencyProfileServices;

@Repository
public interface AgencyProfileServicesRepository extends JpaRepository<AgencyProfileServices, Long> {
    List<AgencyProfileServices> findByAgencyProfile(AgencyProfile agencyProfile);

}