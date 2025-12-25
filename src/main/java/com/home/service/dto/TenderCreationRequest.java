package com.home.service.dto;

import java.time.LocalDateTime;

public class TenderCreationRequest {
    private String title;
    private String description;
    private String location;
    private LocalDateTime closingDate;
    private String contactInfo;
    private Long serviceId;
    private LocalDateTime questionDeadline;
    private Boolean isFree; // optional; defaults to false if null
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

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDateTime closingDate) {
        this.closingDate = closingDate;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getQuestionDeadline() {
        return questionDeadline;
    }

    public void setQuestionDeadline(LocalDateTime questionDeadline) {
        this.questionDeadline = questionDeadline;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNoticeNumber() {
        return noticeNumber;
    }

    public void setNoticeNumber(String noticeNumber) {
        this.noticeNumber = noticeNumber;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getTenderType() {
        return tenderType;
    }

    public void setTenderType(String tenderType) {
        this.tenderType = tenderType;
    }

    public String getProcurementMethod() {
        return procurementMethod;
    }

    public void setProcurementMethod(String procurementMethod) {
        this.procurementMethod = procurementMethod;
    }

    public String getCostOfTenderDocument() {
        return costOfTenderDocument;
    }

    public void setCostOfTenderDocument(String costOfTenderDocument) {
        this.costOfTenderDocument = costOfTenderDocument;
    }

    public String getBidValidity() {
        return bidValidity;
    }

    public void setBidValidity(String bidValidity) {
        this.bidValidity = bidValidity;
    }

    public String getBidSecurity() {
        return bidSecurity;
    }

    public void setBidSecurity(String bidSecurity) {
        this.bidSecurity = bidSecurity;
    }

    public String getContractPeriod() {
        return contractPeriod;
    }

    public void setContractPeriod(String contractPeriod) {
        this.contractPeriod = contractPeriod;
    }

    public String getPerformanceSecurity() {
        return performanceSecurity;
    }

    public void setPerformanceSecurity(String performanceSecurity) {
        this.performanceSecurity = performanceSecurity;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getKeyDeliverables() {
        return keyDeliverables;
    }

    public void setKeyDeliverables(String keyDeliverables) {
        this.keyDeliverables = keyDeliverables;
    }

    public String getTechnicalSpecifications() {
        return technicalSpecifications;
    }

    public void setTechnicalSpecifications(String technicalSpecifications) {
        this.technicalSpecifications = technicalSpecifications;
    }

    public String getTenderReferenceNoticeNo() {
        return tenderReferenceNoticeNo;
    }

    public void setTenderReferenceNoticeNo(String tenderReferenceNoticeNo) {
        this.tenderReferenceNoticeNo = tenderReferenceNoticeNo;
    }

    public String getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(String publishedOn) {
        this.publishedOn = publishedOn;
    }

    public LocalDateTime getBidSubmissionDeadline() {
        return bidSubmissionDeadline;
    }

    public void setBidSubmissionDeadline(LocalDateTime bidSubmissionDeadline) {
        this.bidSubmissionDeadline = bidSubmissionDeadline;
    }

    public String getTenderNoticeCode() {
        return tenderNoticeCode;
    }

    public void setTenderNoticeCode(String tenderNoticeCode) {
        this.tenderNoticeCode = tenderNoticeCode;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
    }

    public String getGeneralEligibility() {
        return generalEligibility;
    }

    public void setGeneralEligibility(String generalEligibility) {
        this.generalEligibility = generalEligibility;
    }

    public String getTechnicalRequirements() {
        return technicalRequirements;
    }

    public void setTechnicalRequirements(String technicalRequirements) {
        this.technicalRequirements = technicalRequirements;
    }

    public String getFinancialRequirements() {
        return financialRequirements;
    }

    public void setFinancialRequirements(String financialRequirements) {
        this.financialRequirements = financialRequirements;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getPreBidMeeting() {
        return preBidMeeting;
    }

    public void setPreBidMeeting(String preBidMeeting) {
        this.preBidMeeting = preBidMeeting;
    }

    public String getSiteVisit() {
        return siteVisit;
    }

    public void setSiteVisit(String siteVisit) {
        this.siteVisit = siteVisit;
    }

    public LocalDateTime getDeadlineForClarifications() {
        return deadlineForClarifications;
    }

    public void setDeadlineForClarifications(LocalDateTime deadlineForClarifications) {
        this.deadlineForClarifications = deadlineForClarifications;
    }

    public LocalDateTime getBidOpeningDate() {
        return bidOpeningDate;
    }

    public void setBidOpeningDate(LocalDateTime bidOpeningDate) {
        this.bidOpeningDate = bidOpeningDate;
    }

    public String getTenderDocumentCollectionLocation() {
        return tenderDocumentCollectionLocation;
    }

    public void setTenderDocumentCollectionLocation(String tenderDocumentCollectionLocation) {
        this.tenderDocumentCollectionLocation = tenderDocumentCollectionLocation;
    }

    public String getTenderDocumentCollectionTime() {
        return tenderDocumentCollectionTime;
    }

    public void setTenderDocumentCollectionTime(String tenderDocumentCollectionTime) {
        this.tenderDocumentCollectionTime = tenderDocumentCollectionTime;
    }

    public String getTenderDocumentDownload() {
        return tenderDocumentDownload;
    }

    public void setTenderDocumentDownload(String tenderDocumentDownload) {
        this.tenderDocumentDownload = tenderDocumentDownload;
    }

    public String getBidSubmissionMode() {
        return bidSubmissionMode;
    }

    public void setBidSubmissionMode(String bidSubmissionMode) {
        this.bidSubmissionMode = bidSubmissionMode;
    }

    public String getBidSubmissionAddress() {
        return bidSubmissionAddress;
    }

    public void setBidSubmissionAddress(String bidSubmissionAddress) {
        this.bidSubmissionAddress = bidSubmissionAddress;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTenderLocation() {
        return tenderLocation;
    }

    public void setTenderLocation(String tenderLocation) {
        this.tenderLocation = tenderLocation;
    }

    public String getLanguageOfBids() {
        return languageOfBids;
    }

    public void setLanguageOfBids(String languageOfBids) {
        this.languageOfBids = languageOfBids;
    }

    public String getValidityPeriodOfBids() {
        return validityPeriodOfBids;
    }

    public void setValidityPeriodOfBids(String validityPeriodOfBids) {
        this.validityPeriodOfBids = validityPeriodOfBids;
    }

    public String getGoverningLaw() {
        return governingLaw;
    }

    public void setGoverningLaw(String governingLaw) {
        this.governingLaw = governingLaw;
    }
}
