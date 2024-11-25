package com.home.service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.Set;

import com.home.service.models.enums.EthiopianLanguage;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTranslation extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Services service;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EthiopianLanguage lang = EthiopianLanguage.ENGLISH;

    public Services getService() {
        return service;
    }

    public void setService(Services service) {
        this.service = service;
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

    public EthiopianLanguage getLang() {
        return lang;
    }

    public void setLang(EthiopianLanguage lang) {
        this.lang = lang;
    }
}