package com.home.service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.TelebirrPayment;

public interface TelebirrPaymentRepository extends JpaRepository<TelebirrPayment, Long> {
    Optional<TelebirrPayment> findByMerchantOrderId(String merchantOrderId);

    Optional<TelebirrPayment> findByPrepayId(String prepayId);
}