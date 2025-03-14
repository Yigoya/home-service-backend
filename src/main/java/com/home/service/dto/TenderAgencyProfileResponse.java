package com.home.service.dto;

import java.util.List;

import com.home.service.models.TenderAgencyProfile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TenderAgencyProfileResponse {
    private Long id;
    private Long userId;
    private String companyName;
    private String tinNumber;
    private String businessLicensePath;
    private List<Long> tenderIds;
    private String website;
    private String contactPerson;
    private String verifiedStatus;

    public TenderAgencyProfileResponse(TenderAgencyProfile tenderAgencyProfile) {
        this.id = tenderAgencyProfile.getId();
        this.userId = tenderAgencyProfile.getUser().getId();
        this.companyName = tenderAgencyProfile.getCompanyName();
        this.tinNumber = tenderAgencyProfile.getTinNumber();
        this.businessLicensePath = tenderAgencyProfile.getBusinessLicensePath();
        this.tenderIds = tenderAgencyProfile.getTenders().stream().map(tender -> tender.getId()).toList();
        this.website = tenderAgencyProfile.getWebsite();
        this.contactPerson = tenderAgencyProfile.getContactPerson();
        this.verifiedStatus = tenderAgencyProfile.getVerifiedStatus();
    }
}
