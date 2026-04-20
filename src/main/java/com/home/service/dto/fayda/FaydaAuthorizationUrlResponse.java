package com.home.service.dto.fayda;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FaydaAuthorizationUrlResponse {
    private String authorizationUrl;
    private String state;
    private long expiresInSeconds;
}
