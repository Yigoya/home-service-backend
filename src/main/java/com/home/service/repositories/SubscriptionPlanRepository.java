package com.home.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.SubscriptionPlan;
import com.home.service.models.enums.PlanType;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findByPlanType(PlanType planType);
}
