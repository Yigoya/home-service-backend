package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Education;
public interface EducationRepository extends JpaRepository<Education, Long> {}