package com.home.service.models;

import jakarta.persistence.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.home.service.models.enums.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private String cardNumber;
    private String cardholderName;
    private String expiryDate;

    // @ManyToOne
    // @JoinColumn(name = "billing_address_id")
    // private Address billingAddress;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    private Boolean isDefault;
}
