package com.home.service.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.Service.ChapaPaymentService;
import com.home.service.dto.ChapaCheckoutRequest;
import com.home.service.dto.ChapaCheckoutResponse;

@RestController
@RequestMapping("/payment/chapa")
public class ChapaPaymentController {

    private final ChapaPaymentService chapaPaymentService;

    public ChapaPaymentController(ChapaPaymentService chapaPaymentService) {
        this.chapaPaymentService = chapaPaymentService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ChapaCheckoutResponse> createCheckout(@RequestBody ChapaCheckoutRequest request) {
        return ResponseEntity.ok(chapaPaymentService.createCheckout(request));
    }

    @PostMapping("/callback")
    public ResponseEntity<String> callback(@RequestBody Map<String, Object> payload) {
        chapaPaymentService.handleCallback(payload);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/verify/{txRef}")
    public ResponseEntity<ChapaCheckoutResponse> verify(@PathVariable String txRef) {
        return ResponseEntity.ok(chapaPaymentService.verifyByTxRef(txRef));
    }
}
