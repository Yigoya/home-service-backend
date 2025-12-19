package com.home.service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.home.service.models.enums.TenderStatus;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private String referenceNumber;
    private String noticeNumber;
    private String productCategory;
    private String tenderType;
    private String procurementMethod;
    private String costOfTenderDocument;
    private String bidValidity;
    private String bidSecurity;
    private String contractPeriod;
    private String performanceSecurity;

    @Column(columnDefinition = "TEXT")
    private String paymentTerms;

    @Column(columnDefinition = "TEXT")
    private String keyDeliverables;

    @Column(columnDefinition = "TEXT")
    private String technicalSpecifications;

    private String tenderReferenceNoticeNo;
    private LocalDateTime publishedOn = LocalDateTime.now();
    private LocalDateTime bidSubmissionDeadline;
    private String tenderNoticeCode;

    @Column(columnDefinition = "TEXT")
    private String warranty;

    @Column(columnDefinition = "TEXT")
    private String generalEligibility;

    @Column(columnDefinition = "TEXT")
    private String technicalRequirements;

    @Column(columnDefinition = "TEXT")
    private String financialRequirements;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @Column(columnDefinition = "TEXT")
    private String preBidMeeting;

    @Column(columnDefinition = "TEXT")
    private String siteVisit;

    private LocalDateTime deadlineForClarifications;
    private LocalDateTime bidOpeningDate;
    private String tenderDocumentCollectionLocation;
    private String tenderDocumentCollectionTime;
    private String tenderDocumentDownload;
    private String bidSubmissionMode;

    @Column(columnDefinition = "TEXT")
    private String bidSubmissionAddress;

    private String organization;
    private String department;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String tenderLocation;
    private String languageOfBids;
    private String validityPeriodOfBids;
    private String governingLaw;
    
    @Column(name = "is_free")
    @JsonProperty("isFree")
    private Boolean free = false;

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
