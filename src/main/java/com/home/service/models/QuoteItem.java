package com.home.service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "quote_items")
@Getter
@Setter
public class QuoteItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private BusinessServices service;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double unitPrice;

    @Transient
    public Double getTotal() {
        return quantity * unitPrice;
    }
}