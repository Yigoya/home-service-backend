package com.home.service.models;

import java.util.Map;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private BusinessServices service;

    private Integer quantity;

    private Double unitPrice;
    private Double subtotal;

    @ElementCollection
    @CollectionTable(name = "order_item_options")
    @MapKeyColumn(name = "option_name")
    @Column(name = "selected_choice")
    private Map<String, String> selectedOptions;

    private String notes;
}