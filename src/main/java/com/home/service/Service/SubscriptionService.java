package com.home.service.Service;

import org.springframework.stereotype.Service;

import com.home.service.models.SubscriptionPlan;
import com.home.service.models.enums.PlanType;
import com.home.service.repositories.SubscriptionPlanRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;

    public SubscriptionService(SubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<SubscriptionPlan> getPlansByType(PlanType planType) {
        return planRepository.findByPlanType(planType);
    }

    public SubscriptionPlan getPlanById(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription plan not found: " + planId));
    }
}
