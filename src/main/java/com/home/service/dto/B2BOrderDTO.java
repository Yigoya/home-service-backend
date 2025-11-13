package com.home.service.dto;

import java.time.LocalDateTime;

import com.home.service.models.enums.B2BOrderStatus;

import lombok.Data;

@Data
public class B2BOrderDTO {
    private Long id;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private Integer quantity;
    private Double totalAmount;
    private B2BOrderStatus status;
    private String shippingDetails;
    private LocalDateTime orderDate;
}
