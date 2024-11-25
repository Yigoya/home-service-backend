package com.home.service.dto;

import com.home.service.models.ServiceCategory;
import com.home.service.models.enums.EthiopianLanguage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCategoryDTO {

    private String categoryName;

    private String description;

    private EthiopianLanguage lang;

    public ServiceCategoryDTO(ServiceCategory serviceCategory, EthiopianLanguage lang) {

        this.categoryName = serviceCategory.getTranslations().stream()
                .filter(translation -> translation.getLang().equals(lang))
                .findFirst()
                .orElseGet(() -> serviceCategory.getTranslations().stream()
                        .filter(translation -> translation.getLang().equals(EthiopianLanguage.ENGLISH))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No translation found for language: " + lang + " or ENGLISH")))
                .getName();
        this.description = serviceCategory.getTranslations().stream()
                .filter(translation -> translation.getLang().equals(lang))
                .findFirst()
                .orElseGet(() -> serviceCategory.getTranslations().stream()
                        .filter(translation -> translation.getLang().equals(EthiopianLanguage.ENGLISH))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No translation found for language: " + lang + " or ENGLISH")))
                .getDescription();
        this.lang = serviceCategory.getTranslations().stream()
                .filter(translation -> translation.getLang().equals(lang))
                .findFirst()
                .orElseGet(() -> serviceCategory.getTranslations().stream()
                        .filter(translation -> translation.getLang().equals(EthiopianLanguage.ENGLISH))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No translation found for language: " + lang + " or ENGLISH")))
                .getLang();

    }
}