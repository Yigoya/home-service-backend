package com.home.service.dto;

import com.home.service.models.SavedBusiness;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class SavedBusinessSummaryDto {
    private Long id;
    private Long businessId;
    private String businessName;
    private String businessLogo;
    private String businessEmail;
    private String businessPhoneNumber;
    private String businessIndustry;
    private String businessDescription;
    private boolean isVerified;
    private boolean isFeatured;
    private Instant savedAt;

    public SavedBusinessSummaryDto(SavedBusiness savedBusiness) {
        this.id = savedBusiness.getId();
        this.businessId = savedBusiness.getBusiness().getId();
        this.businessName = savedBusiness.getBusiness().getName();
        this.businessLogo = savedBusiness.getBusiness().getLogo();
        this.businessEmail = savedBusiness.getBusiness().getEmail();
        this.businessPhoneNumber = savedBusiness.getBusiness().getPhoneNumber();
        this.businessIndustry = savedBusiness.getBusiness().getIndustry();
        this.businessDescription = savedBusiness.getBusiness().getDescription();
        this.isVerified = savedBusiness.getBusiness().isVerified();
        this.isFeatured = savedBusiness.getBusiness().isFeatured();
        this.savedAt = savedBusiness.getSavedAt();
    }
}