package com.home.service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.home.service.models.enums.B2BPartnerStatus;
import com.home.service.models.enums.B2BPartnerType;

@Entity
@Table(name = "b2b_partners")
@Getter
@Setter
public class B2BPartner extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne
    @JoinColumn(name = "partner_business_id", nullable = false)
    private Business partnerBusiness;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private B2BPartnerType partnerType;

    private Double creditLimit;

    private String paymentTerms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private B2BPartnerStatus status = B2BPartnerStatus.PENDING;
}