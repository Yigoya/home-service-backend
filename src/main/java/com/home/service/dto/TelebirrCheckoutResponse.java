package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TelebirrCheckoutResponse {
    private String checkoutUrl;
    private String merchantOrderId;
    private String prepayId;
    private String status;
}