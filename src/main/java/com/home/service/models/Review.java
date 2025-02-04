package com.home.service.models;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Technician technician;

    private Integer rating;
    private String reviewText;
}
