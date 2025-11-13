package com.home.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.home.service.models.enums.LocationType;
import com.home.service.models.enums.OrderStatus;
import com.home.service.models.enums.PaymentStatus;
import com.home.service.Service.OrderService.OrderItemDTO;
import com.home.service.models.enums.Coordinates;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {

    private Long customerId;
    private Long businessId;
    private OrderStatus status;
    private LocalDateTime scheduledDate;
    private String specialInstructions;
    private Long paymentMethodId;
    private String orderNumber;
    private List<OrderItemDTO> items;

    // Business Location Attributes
    private String name;
    private LocationType type;
    private Coordinates coordinates;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}