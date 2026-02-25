package com.home.service.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.TelebirrPaymentService;
import com.home.service.dto.TelebirrCheckoutRequest;
import com.home.service.dto.TelebirrCheckoutResponse;

@RestController
@RequestMapping("/payment/telebirr")
public class PaymentController {

    private final TelebirrPaymentService telebirrPaymentService;

    public PaymentController(TelebirrPaymentService telebirrPaymentService) {
        this.telebirrPaymentService = telebirrPaymentService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<TelebirrCheckoutResponse> createCheckout(
            @RequestBody TelebirrCheckoutRequest request) {
        return ResponseEntity.ok(telebirrPaymentService.createCheckout(request));
    }

    @PostMapping("/notify")
    public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> payload) {
        telebirrPaymentService.handleCallback(payload);
        return ResponseEntity.ok("OK");
    }
}
