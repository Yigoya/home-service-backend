package com.home.service.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.PaymentMethod;
import com.home.service.models.User;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Page<PaymentMethod> findByUser(User user, Pageable pageable);

    Optional<PaymentMethod> findByUserAndIsDefaultTrue(User user);
}