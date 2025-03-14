package com.home.service.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.PaymentMethodService;
import com.home.service.Service.PaymentMethodService.PaymentMethodDTO;
import com.home.service.models.PaymentMethod;

@RestController
@RequestMapping("/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @PostMapping
    public ResponseEntity<PaymentMethod> createPaymentMethod(@RequestBody PaymentMethodDTO paymentMethodDTO) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        PaymentMethod paymentMethod = paymentMethodService.createPaymentMethod(paymentMethodDTO, currentUserId);
        return new ResponseEntity<>(paymentMethod, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethod> getPaymentMethodById(@PathVariable Long id) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodById(id, currentUserId);
        return ResponseEntity.ok(paymentMethod);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentMethod>> getUserPaymentMethods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        Page<PaymentMethod> paymentMethods = paymentMethodService.getUserPaymentMethods(currentUserId, page, size);
        return ResponseEntity.ok(paymentMethods);
    }

    @GetMapping("/default")
    public ResponseEntity<PaymentMethod> getDefaultPaymentMethod() {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        PaymentMethod paymentMethod = paymentMethodService.getDefaultPaymentMethod(currentUserId);
        return ResponseEntity.ok(paymentMethod);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethod> updatePaymentMethod(
            @PathVariable Long id,
            @RequestBody PaymentMethodDTO paymentMethodDTO) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        PaymentMethod paymentMethod = paymentMethodService.updatePaymentMethod(id, paymentMethodDTO, currentUserId);
        return ResponseEntity.ok(paymentMethod);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long id) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        paymentMethodService.deletePaymentMethod(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<PaymentMethod> setDefaultPaymentMethod(@PathVariable Long id) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        PaymentMethod paymentMethod = paymentMethodService.setDefaultPaymentMethod(id, currentUserId);
        return ResponseEntity.ok(paymentMethod);
    }
}