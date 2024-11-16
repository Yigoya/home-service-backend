package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Operator;

public interface OperatorRepository extends JpaRepository<Operator, Long> {
}
