package com.home.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerId(Long customerId);
}
