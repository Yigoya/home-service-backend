package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Customer;
import com.home.service.models.SavedBusiness;

import java.util.List;
import java.util.Optional;

public interface SavedBusinessRepository extends JpaRepository<SavedBusiness, Long> {
    
    // Find all businesses saved by a specific customer
    List<SavedBusiness> findByCustomer(Customer customer);
    
    // Find all businesses saved by a specific customer with pagination
    Page<SavedBusiness> findByCustomer(Customer customer, Pageable pageable);
    
    // Find a specific saved business entry to delete it
    Optional<SavedBusiness> findByCustomerAndBusinessId(Customer customer, Long businessId);
    
    // Check if a business is already saved to prevent duplicates
    boolean existsByCustomerAndBusinessId(Customer customer, Long businessId);
}