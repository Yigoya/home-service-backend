package com.home.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.CustomerSubscription;
import com.home.service.models.enums.SubscriptionStatus;

@Repository
public interface CustomerSubscriptionRepository extends JpaRepository<CustomerSubscription, Long> {
    List<CustomerSubscription> findByFollowedServiceIdsContainingAndStatus(Long serviceId, SubscriptionStatus status);
}
