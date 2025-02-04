package com.home.service.models;

import jakarta.persistence.*;
import com.home.service.models.enums.*;
import lombok.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Technician technician;

    @ManyToOne
    @JoinColumn(name = "service_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Services service;

    private LocalDateTime scheduledDate;
    private String description;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "service_location_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
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
