package com.home.service.dto;

import lombok.Data;

@Data
public class VerifyResponseData {
    private String status;
    private String message;
    private String txRef;
}
