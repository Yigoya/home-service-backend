package com.home.service.dto;

import com.home.service.models.SavedBusiness;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class SavedBusinessDto {
    private Long id;
    private Long customerId;
    private BusinessDTO business;
    private Instant savedAt;

    public SavedBusinessDto(SavedBusiness savedBusiness) {
        this.id = savedBusiness.getId();
        this.customerId = savedBusiness.getCustomer().getId();
        this.business = new BusinessDTO(savedBusiness.getBusiness());
        this.savedAt = savedBusiness.getSavedAt();
    }
}