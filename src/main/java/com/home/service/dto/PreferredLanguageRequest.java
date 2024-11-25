package com.home.service.dto;

import com.home.service.models.enums.EthiopianLanguage;

import jakarta.validation.constraints.NotNull;

public class PreferredLanguageRequest {
    @NotNull(message = "Preferred language is required")
    private EthiopianLanguage preferredLanguage;

    // Getters and setters
    public EthiopianLanguage getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(EthiopianLanguage preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
}
