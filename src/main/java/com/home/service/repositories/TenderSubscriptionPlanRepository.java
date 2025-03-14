package com.home.service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.TenderSubscriptionPlan;

@Repository
public interface TenderSubscriptionPlanRepository extends JpaRepository<TenderSubscriptionPlan, Long> {
    Optional<TenderSubscriptionPlan> findByPlanId(String planId);
}
