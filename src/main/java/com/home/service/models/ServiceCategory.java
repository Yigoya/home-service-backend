package com.home.service.models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategory extends BaseEntity {

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ServiceCategoryTranslation> translations = new HashSet<>();

    private String icon;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Set<ServiceCategoryTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(Set<ServiceCategoryTranslation> translations) {
        this.translations = translations;
    }
}
