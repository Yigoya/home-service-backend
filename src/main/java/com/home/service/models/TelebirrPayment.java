package com.home.service.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.home.service.models.enums.PlanType;
import com.home.service.models.enums.SubscriberType;
import com.home.service.models.enums.TelebirrPaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "telebirr_payments")
@Getter
@Setter
public class TelebirrPayment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriberType subscriberType;

    @Column(nullable = false)
    private Long subscriberId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "ETB";

    @Column(unique = true)
    private String merchantOrderId;

    @Column
    private String prepayId;

    @Column(columnDefinition = "TEXT")
    private String checkoutUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TelebirrPaymentStatus status = TelebirrPaymentStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rawRequest;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;

    @Column(columnDefinition = "TEXT")
    private String callbackPayload;

    private LocalDateTime callbackReceivedAt;

    private String errorMessage;
}