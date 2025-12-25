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
    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String location;
    private LocalDateTime datePosted = LocalDateTime.now();
    private LocalDateTime closingDate;
    @Column(columnDefinition = "TEXT")
    private String contactInfo;

    @Column(columnDefinition = "TEXT")
    private String referenceNumber;

    @Column(columnDefinition = "TEXT")
    private String noticeNumber;

    @Column(columnDefinition = "TEXT")
    private String productCategory;

    @Column(columnDefinition = "TEXT")
    private String tenderType;

    @Column(columnDefinition = "TEXT")
    private String procurementMethod;

    @Column(columnDefinition = "TEXT")
    private String costOfTenderDocument;

    @Column(columnDefinition = "TEXT")
    private String bidValidity;

    @Column(columnDefinition = "TEXT")
    private String bidSecurity;

    @Column(columnDefinition = "TEXT")
    private String contractPeriod;

    @Column(columnDefinition = "TEXT")
    private String performanceSecurity;

    @Column(columnDefinition = "TEXT")
    private String paymentTerms;

    @Column(columnDefinition = "TEXT")
    private String keyDeliverables;

    @Column(columnDefinition = "TEXT")
    private String technicalSpecifications;

    @Column(columnDefinition = "TEXT")
    private String tenderReferenceNoticeNo;

    @Column(columnDefinition = "TEXT")
    private String publishedOn;
    private LocalDateTime bidSubmissionDeadline;
    @Column(columnDefinition = "TEXT")
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
    @Column(columnDefinition = "TEXT")
    private String tenderDocumentCollectionLocation;

    @Column(columnDefinition = "TEXT")
    private String tenderDocumentCollectionTime;

    @Column(columnDefinition = "TEXT")
    private String tenderDocumentDownload;

    @Column(columnDefinition = "TEXT")
    private String bidSubmissionMode;

    @Column(columnDefinition = "TEXT")
    private String bidSubmissionAddress;

    @Column(columnDefinition = "TEXT")
    private String organization;

    @Column(columnDefinition = "TEXT")
    private String department;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String tenderLocation;

    @Column(columnDefinition = "TEXT")
    private String languageOfBids;

    @Column(columnDefinition = "TEXT")
    private String validityPeriodOfBids;

    @Column(columnDefinition = "TEXT")
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

    @Column(columnDefinition = "TEXT")
    private String documentPath;
    private LocalDateTime questionDeadline = LocalDateTime.now().plusWeeks(2);
}
