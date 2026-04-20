package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChapaCheckoutResponse {
    private String checkoutUrl;
    private String txRef;
    private String status;
}
