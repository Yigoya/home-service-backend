package com.home.service.dto;

import lombok.Data;

@Data
public class InitializeResponseData {
    private String message;
    private String checkoutUrl;
    private String txRef;
}
