package com.home.service.dto;

import java.time.LocalTime;

import org.checkerframework.checker.units.qual.t;

import com.home.service.models.Services;
import com.home.service.models.enums.EthiopianLanguage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private LocalTime duration;

    public ServiceDTO(Services service, EthiopianLanguage language) {

        // Initialize fields using the service object
        this.id = service.getId();
        this.name = service.getTranslations().stream().filter(translation -> translation.getLang().equals(language))
                .findFirst().orElseGet(() -> service.getTranslations().stream()
                        .filter(translation -> translation.getLang().equals(EthiopianLanguage.ENGLISH))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException(
                                "No translation found for language: " + language + " or ENGLISH")))
                .getName();
        this.description = service.getTranslations().stream()
                .filter(translation -> translation.getLang().equals(language)).findFirst()
                .orElseGet(() -> service.getTranslations().stream()
                        .filter(translation -> translation.getLang().equals(EthiopianLanguage.ENGLISH))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No translation found for language: " + language + " or ENGLISH")))
                .getDescription();
        ;
        this.price = service.getServiceFee();
        this.duration = service.getEstimatedDuration();

    }
}
