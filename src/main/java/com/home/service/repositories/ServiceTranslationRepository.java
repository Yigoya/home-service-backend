package com.home.service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.ServiceTranslation;

public interface ServiceTranslationRepository extends JpaRepository<ServiceTranslation, Long> {
    Optional<ServiceTranslation> findFirstByNameIgnoreCaseAndService_Category_IdAndService_ServiceIdIsNull(String name, Long categoryId);

    Optional<ServiceTranslation> findFirstByNameIgnoreCaseAndService_Category_IdAndService_ServiceId(String name, Long categoryId, Long parentServiceId);

    Optional<ServiceTranslation> findFirstByNameIgnoreCase(String name);
}
