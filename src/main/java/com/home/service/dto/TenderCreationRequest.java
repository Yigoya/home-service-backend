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
}
