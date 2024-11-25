package com.home.service.models;

import com.home.service.models.enums.EthiopianLanguage;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryTranslation extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EthiopianLanguage lang = EthiopianLanguage.ENGLISH;

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
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
