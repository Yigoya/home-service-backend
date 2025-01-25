package com.home.service.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentRequest {
    private String bankCode;
    private String accountNumber;
    private BigDecimal amount;
    private String email;
    private String firstName;
    private String lastName;
}
