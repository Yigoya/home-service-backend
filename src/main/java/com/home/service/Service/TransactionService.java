package com.home.service.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.home.service.models.Customer;
import com.home.service.models.Transaction;
import com.home.service.models.enums.TransactionType;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.TransactionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public void purchaseCoins(Long customerId, Integer coinAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        // Update customer coin balance
        customer.setCoinBalance(customer.getCoinBalance() + coinAmount);
        customerRepository.save(customer);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(coinAmount);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription("Coins purchased");
        transaction.setTransactionType(TransactionType.PURCHASE);

        transactionRepository.save(transaction);
    }

    public void deductCoins(Long customerId, Integer coinAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        if (customer.getCoinBalance() < coinAmount) {
            throw new IllegalArgumentException("Insufficient coins");
        }

        // Deduct coins and save
        customer.setCoinBalance(customer.getCoinBalance() - coinAmount);
        customerRepository.save(customer);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(-coinAmount);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription("Coins used for booking");
        transaction.setTransactionType(TransactionType.USAGE);

        transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactions(Long customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }

    public Integer getBalance(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        return customer.getCoinBalance();
    }
}
