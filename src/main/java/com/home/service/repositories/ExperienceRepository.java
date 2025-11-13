package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Experience;
public interface ExperienceRepository extends JpaRepository<Experience, Long> {}
