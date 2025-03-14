package com.home.service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.home.service.models.enums.QuoteStatus;

@Entity
@Table(name = "quotes")
@Getter
@Setter
public class Quote extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private B2BPartner partner;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteItem> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate validUntil;

    @Column(length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Column(nullable = false)
    private Double totalAmount = 0.0;

    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }

    public void addItem(QuoteItem item) {
        items.add(item);
        item.setQuote(this);
        calculateTotalAmount();
    }

    public void removeItem(QuoteItem item) {
        items.remove(item);
        item.setQuote(null);
        calculateTotalAmount();
    }
}