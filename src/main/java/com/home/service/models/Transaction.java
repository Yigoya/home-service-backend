package com.home.service.models;

import java.time.LocalDateTime;

import com.home.service.models.enums.TransactionType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private Integer amount; // Positive for purchase, negative for usage
    private LocalDateTime transactionDate;
    private String description; // e.g., "Coins purchased" or "Booking payment"

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // PURCHASE, USAGE
}
