package com.home.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.TechnicianPortfolio;

public interface TechnicianPortfolioRepository extends JpaRepository<TechnicianPortfolio, Long> {
    List<TechnicianPortfolio> findByTechnician_Id(Long technicianId);
}
