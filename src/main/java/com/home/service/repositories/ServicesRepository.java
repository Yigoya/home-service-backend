package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.Services;

@Repository
public interface ServicesRepository extends JpaRepository<Services, Long> {
}