package com.home.service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Operator;
import com.home.service.models.User;

public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByUser_Id(Long id);
}
