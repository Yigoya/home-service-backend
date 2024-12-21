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
        private Long id;

        private String categoryName;

        private String description;

        private EthiopianLanguage lang;
        private String icon;

        public ServiceCategoryDTO(ServiceCategory serviceCategory, EthiopianLanguage lang) {
                this.id = serviceCategory.getId();

                this.categoryName = serviceCategory.getTranslations().stream()
                                .filter(translation -> translation.getLang().equals(lang))
                                .findFirst()
                                .orElseGet(() -> serviceCategory.getTranslations().stream()
                                                .filter(translation -> translation.getLang()
                                                                .equals(EthiopianLanguage.ENGLISH))
                                                .findFirst()
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "No translation found for language: " + lang
                                                                                + " or ENGLISH")))
                                .getName();
                this.description = serviceCategory.getTranslations().stream()
                                .filter(translation -> translation.getLang().equals(lang))
                                .findFirst()
                                .orElseGet(() -> serviceCategory.getTranslations().stream()
                                                .filter(translation -> translation.getLang()
                                                                .equals(EthiopianLanguage.ENGLISH))
                                                .findFirst()
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "No translation found for language: " + lang
                                                                                + " or ENGLISH")))
                                .getDescription();
                this.lang = serviceCategory.getTranslations().stream()
                                .filter(translation -> translation.getLang().equals(lang))
                                .findFirst()
                                .orElseGet(() -> serviceCategory.getTranslations().stream()
                                                .filter(translation -> translation.getLang()
                                                                .equals(EthiopianLanguage.ENGLISH))
                                                .findFirst()
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "No translation found for language: " + lang
                                                                                + " or ENGLISH")))
                                .getLang();
                this.icon = serviceCategory.getIcon();

        }
}