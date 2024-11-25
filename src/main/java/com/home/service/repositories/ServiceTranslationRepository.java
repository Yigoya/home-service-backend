package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.home.service.models.ServiceTranslation;

public interface ServiceTranslationRepository extends JpaRepository<ServiceTranslation, Long> {

}
