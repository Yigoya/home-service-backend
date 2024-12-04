package com.home.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PurchaseCoinsRequest {
    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotNull(message = "Coin amount cannot be null")
    @Min(value = 1, message = "Coin amount must be at least 1")
    private Integer coinAmount;

    // Getters and setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Integer getCoinAmount() {
        return coinAmount;
    }

    public void setCoinAmount(Integer coinAmount) {
        this.coinAmount = coinAmount;
    }
}
