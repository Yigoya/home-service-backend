package com.home.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.Service.TransactionService;
import com.home.service.dto.DeductCoinsRequest;
import com.home.service.dto.PurchaseCoinsRequest;
import com.home.service.models.Transaction;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/coins")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseCoins(@Valid @RequestBody PurchaseCoinsRequest request) {
        transactionService.purchaseCoins(request.getCustomerId(), request.getCoinAmount());
        return ResponseEntity.ok("Coins purchased successfully");
    }

    @PostMapping("/deduct")
    public ResponseEntity<String> deductCoins(@Valid @RequestBody DeductCoinsRequest request) {
        transactionService.deductCoins(request.getCustomerId(), request.getCoinAmount());
        return ResponseEntity.ok("Coins deducted successfully");
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@RequestParam Long customerId) {
        return ResponseEntity.ok(transactionService.getTransactions(customerId));
    }

    @GetMapping("/balance/{customerId}")
    public ResponseEntity<Integer> getBalance(@PathVariable Long customerId) {
        return ResponseEntity.ok(transactionService.getBalance(customerId));
    }
}
