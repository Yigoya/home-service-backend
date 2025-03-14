package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.Order;
import com.home.service.models.Business;
import com.home.service.models.Customer;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    Page<Order> findByBusiness(Business business, Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);
}