package com.home.service.models;

import java.time.LocalDateTime;

import com.google.firebase.database.annotations.NotNull;
import com.home.service.models.enums.B2BOrderStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "b2b_orders")
@Getter
@Setter
public class B2BOrder extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private Business buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Business seller;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    private Integer quantity;

    @NotNull
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private B2BOrderStatus status = B2BOrderStatus.PENDING;

    private String shippingDetails;

    private LocalDateTime orderDate;
}
