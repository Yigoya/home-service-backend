package com.home.service.models;

import jakarta.persistence.*;
import com.home.service.models.enums.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dispute extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    private String reason;

    private String description;

    @Enumerated(EnumType.STRING)
    private DisputeStatus status;
}
