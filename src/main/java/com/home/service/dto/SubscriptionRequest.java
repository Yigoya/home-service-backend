package com.home.service.dto;

import java.util.Set;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class SubscriptionRequest {
    private String planId;
    private String whatsappNumber;
    private String telegramUsername;
    private Set<Long> serviceIds;
    private String companyName;
    private String tinNumber;
}