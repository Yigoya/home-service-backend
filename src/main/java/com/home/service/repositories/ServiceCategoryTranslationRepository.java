package com.home.service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceCategoryTranslation;
import com.home.service.models.enums.EthiopianLanguage;

public interface ServiceCategoryTranslationRepository extends JpaRepository<ServiceCategoryTranslation, Long> {
    Optional<ServiceCategoryTranslation> findByCategoryAndLang(ServiceCategory category, EthiopianLanguage lang);

}
