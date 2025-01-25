package com.home.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.ChapaService;
import com.home.service.dto.Bank;
import com.home.service.dto.PaymentRequest;
import com.home.service.dto.VerifyResponseData;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private ChapaService chapaService;

    @PostMapping("/initialize")
    public String initializePayment(@RequestBody PaymentRequest paymentRequest) {
        return chapaService.initializePayment(paymentRequest);
    }

    @GetMapping("/verify/{txRef}")
    public com.yaphet.chapa.model.VerifyResponseData verifyPayment(@PathVariable String txRef) {
        return chapaService.verifyPayment(txRef);
    }

    @GetMapping("/banks")
    public List<com.yaphet.chapa.model.Bank> getBanks() {
        return chapaService.getBanks();
    }
}
