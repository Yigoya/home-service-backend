package com.home.service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity

@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "translations")
public class Services extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @ManyToMany(mappedBy = "services")
    private Set<Question> questions;

    private LocalTime estimatedDuration;
    private Double serviceFee;

    @Transient
    private Long categoryId;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ServiceTranslation> translations = new HashSet<>();

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
}