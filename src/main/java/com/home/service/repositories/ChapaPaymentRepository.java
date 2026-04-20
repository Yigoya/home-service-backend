package com.home.service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.ChapaPayment;

public interface ChapaPaymentRepository extends JpaRepository<ChapaPayment, Long> {
    Optional<ChapaPayment> findByTxRef(String txRef);
}
