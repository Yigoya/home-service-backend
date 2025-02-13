package com.home.service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Services extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private ServiceCategory category;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = true)
    private AgencyProfile agency;

    @ManyToMany(mappedBy = "services")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Set<Question> questions;

    private LocalTime estimatedDuration;
    private Double serviceFee;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    Set<ServiceTranslation> translations = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_id")
    private Set<Services> services = new HashSet<>();

    @Transient
    private Long categoryId;

    @Column(name = "service_id")
    private Long serviceId;

    private String icon;

    private String document;

    public boolean hasParentService() {
        return serviceId != null;
    }

    @ManyToOne
    @JoinColumn(name = "mobile_category_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private ServiceCategory mobileCategory;

    @Transient
    private Long mobileCategoryId;

    public ServiceCategory getMobileCategory() {
        return mobileCategory;
    }

    public void setMobileCategory(ServiceCategory mobileCategory) {
        this.mobileCategory = mobileCategory;
    }

    public Long getMobileCategoryId() {
        return mobileCategoryId;
    }

    public void setMobileCategoryId(Long mobileCategoryId) {
        this.mobileCategoryId = mobileCategoryId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public LocalTime getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(LocalTime estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(Double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Set<ServiceTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(Set<ServiceTranslation> translations) {
        this.translations = translations;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public Set<Services> getServices() {
        return services;
    }

    public void setServices(Set<Services> services) {
        this.services = services;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public AgencyProfile getAgency() {
        return agency;
    }

    public void setAgency(AgencyProfile agency) {
        this.agency = agency;
    }
}