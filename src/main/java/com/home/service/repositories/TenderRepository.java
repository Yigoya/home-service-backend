package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Tender;
import com.home.service.models.enums.TenderStatus;

import java.util.List;

public interface TenderRepository extends JpaRepository<Tender, Long> {
    List<Tender> findByServiceId(Long serviceId);

    List<Tender> findByStatus(TenderStatus status);

    Page<Tender> findByStatus(TenderStatus status, Pageable pageable);

    List<Tender> findByServiceIdIn(List<Long> serviceIds);

    Page<Tender> findByServiceIdIn(List<Long> serviceIds, Pageable pageable);

    Page<Tender> findByLocationAndServiceIdIn(String location, List<Long> serviceIds, Pageable pageable);

    Page<Tender> findByLocation(String location, Pageable pageable);
}