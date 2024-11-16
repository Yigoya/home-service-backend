package com.home.service.models;

import jakarta.persistence.*;
import com.home.service.models.enums.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Services service;

    private LocalDateTime scheduledDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "service_location_id")
    private Address serviceLocation;

    private Double totalCost;

    @Transient
    private Long customerId;

    @Transient
    private Long technicianId;

    @Transient
    private Long serviceId;

    private String timeSchedule; // New field to store time schedule as a string
}
