package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.models.Customer;
import com.home.service.models.SubscriptionPlan;
import com.home.service.models.User;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.subscriptionPlan = :plan WHERE c.id = :customerId")
    int updateSubscriptionPlanById(@Param("customerId") Long customerId, @Param("plan") SubscriptionPlan plan);

    Optional<Customer> findByUser(User user);
    Optional<Customer> findByUserId(Long userId);
}