package com.home.service.dto;

import com.home.service.models.TechnicianPortfolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianPortfolioDTO {
    private Long id;
    private String description;
    private String beforeImage;
    private String afterImage;

    public TechnicianPortfolioDTO(TechnicianPortfolio entity) {
        this.id = entity.getId();
        this.description = entity.getDescription();
        this.beforeImage = entity.getBeforeImage();
        this.afterImage = entity.getAfterImage();
    }
}
