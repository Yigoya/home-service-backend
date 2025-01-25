package com.home.service.Service;

import com.home.service.dto.PaymentRequest;
import com.yaphet.chapa.Chapa;
import com.yaphet.chapa.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ChapaService {

    @Value("${chapa.api.key}")
    private String chapaApiKey;

    @Value("${chapa.callback.url}")
    private String callbackUrl;

    @Value("${chapa.return.url}")
    private String returnUrl;

    private Chapa chapa;

    public ChapaService() {
        this.chapa = new Chapa("CHASECK_TEST-cR8ncgOLLPA53khMS88AqHQU3jPtBRO4");
    }

    public String initializePayment(PaymentRequest paymentRequest) {
        // Prepare payment data
        PostData postData = new PostData()
                .setAmount(paymentRequest.getAmount())
                .setCurrency("ETB")
                .setEmail(paymentRequest.getEmail())
                .setFirstName(paymentRequest.getFirstName())
                .setLastName(paymentRequest.getLastName())
                .setTxRef("TX-" + System.currentTimeMillis())
                .setCallbackUrl(callbackUrl)
                .setReturnUrl(returnUrl);

        try {
            // Initialize payment
            InitializeResponseData responseData = chapa.initialize(postData);
            return responseData.getData().getCheckOutUrl();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize payment: " + e.getMessage());
        }
    }

    public VerifyResponseData verifyPayment(String txRef) {
        try {
            // Verify payment
            return chapa.verify(txRef);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to verify payment: " + e.getMessage());
        }
    }

    public List<Bank> getBanks() {
        try {
            System.out.println(chapa.banks());
            return chapa.banks();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to fetch banks: " + e.getMessage());
        }
    }
}