package com.home.service.dto;

import java.time.LocalDateTime;

import com.home.service.models.enums.TenderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenderResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime datePosted;
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
    private String paymentTerms;
    private String keyDeliverables;
    private String technicalSpecifications;
    private TenderStatus status;
    private Long serviceId;
    private String documentPath;
    private LocalDateTime questionDeadline;
    @com.fasterxml.jackson.annotation.JsonProperty("isFree")
    private boolean free;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(LocalDateTime datePosted) {
        this.datePosted = datePosted;
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

    public TenderStatus getStatus() {
        return status;
    }

    public void setStatus(TenderStatus status) {
        this.status = status;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public LocalDateTime getQuestionDeadline() {
        return questionDeadline;
    }

    public void setQuestionDeadline(LocalDateTime questionDeadline) {
        this.questionDeadline = questionDeadline;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }
}
