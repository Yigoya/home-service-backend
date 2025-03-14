package com.home.service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.home.service.models.enums.TenderStatus;

import java.time.LocalDateTime;

import org.checkerframework.checker.units.qual.C;

@Entity
@Getter
@Setter
public class Tender extends BaseEntity {
    private String title;
    private String description;
    private String location;
    private LocalDateTime datePosted = LocalDateTime.now();
    private LocalDateTime closingDate;
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    private TenderStatus status = TenderStatus.OPEN;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Services service;

    @Column(name = "service_id", insertable = false, updatable = false)
    private Long serviceId;

    @ManyToOne
    @JoinColumn(name = "agency_id")
    private TenderAgencyProfile agency;

    @Column(name = "agency_id", insertable = false, updatable = false)
    private Long agencyId;

    private String documentPath;
    private LocalDateTime questionDeadline = LocalDateTime.now().plusWeeks(2);
}
