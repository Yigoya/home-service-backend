package com.home.service.dto;

import java.time.LocalTime;

import org.springframework.web.multipart.MultipartFile;

import com.home.service.models.enums.EthiopianLanguage;

public class AgencyServiceRequest {
    private String name;
    private String description;
    private LocalTime estimatedDuration;
    private Double serviceFee;
    private EthiopianLanguage lang;
    private MultipartFile icon;

    public EthiopianLanguage getLang() {
        return lang;
    }

    public void setLang(EthiopianLanguage lang) {
        this.lang = lang;
    }

    public MultipartFile getIcon() {
        return icon;
    }

    public void setIcon(MultipartFile icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}