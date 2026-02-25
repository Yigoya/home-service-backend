package com.home.service.dto;

import com.home.service.models.enums.SubscriberType;

import lombok.Data;

@Data
public class TelebirrCheckoutRequest {
    private Long planId;
    private SubscriberType subscriberType;
    private Long subscriberId;
}