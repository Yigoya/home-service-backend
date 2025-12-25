
package com.home.service.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import com.home.service.models.enums.TenderStatus;

@Getter
@Setter
public class TenderRequest {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime closingDate;
    private String contactInfo;
    private TenderStatus status;
    private Long categoryId;
    private Boolean isFree; // optional, defaults to false when not provided
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
    private String paymentTerms;
    private String keyDeliverables;
    private String technicalSpecifications;
    private String tenderReferenceNoticeNo;
    private String publishedOn;
    private LocalDateTime bidSubmissionDeadline;
    private String tenderNoticeCode;
    private String warranty;
    private String generalEligibility;
    private String technicalRequirements;
    private String financialRequirements;
    private String experience;
    private String preBidMeeting;
    private String siteVisit;
    private LocalDateTime deadlineForClarifications;
    private LocalDateTime bidOpeningDate;
    private String tenderDocumentCollectionLocation;
    private String tenderDocumentCollectionTime;
    private String tenderDocumentDownload;
    private String bidSubmissionMode;
    private String bidSubmissionAddress;
    private String organization;
    private String department;
    private String address;
    private String tenderLocation;
    private String languageOfBids;
    private String validityPeriodOfBids;
    private String governingLaw;

    private MultipartFile file;
}
