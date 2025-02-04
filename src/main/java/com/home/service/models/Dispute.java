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
public class Dispute extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Technician technician;

    private String reason;

    private String description;

    @Enumerated(EnumType.STRING)
    private DisputeStatus status;
}
