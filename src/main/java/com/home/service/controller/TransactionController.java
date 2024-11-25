package com.home.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.Service.TransactionService;
import com.home.service.models.Transaction;

@RestController
@RequestMapping("/coins")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseCoins(@RequestParam Long customerId, @RequestParam Integer coinAmount) {
        transactionService.purchaseCoins(customerId, coinAmount);
        return ResponseEntity.ok("Coins purchased successfully");
    }

    @PostMapping("/deduct")
    public ResponseEntity<String> deductCoins(@RequestParam Long customerId, @RequestParam Integer coinAmount) {
        transactionService.deductCoins(customerId, coinAmount);
        return ResponseEntity.ok("Coins deducted successfully");
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@RequestParam Long customerId) {
        return ResponseEntity.ok(transactionService.getTransactions(customerId));
    }
}
