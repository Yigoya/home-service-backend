package com.home.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.home.service.models.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCustomerId(Long customerId);

}
