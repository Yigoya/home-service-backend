package com.home.service.dto;

import java.util.Collections;
import java.util.List;

import com.home.service.models.TenderAgencyProfile;
import com.home.service.models.Tender;

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
        List<Tender> agencyTenders = tenderAgencyProfile.getTenders() == null
            ? Collections.<Tender>emptyList()
            : tenderAgencyProfile.getTenders();
        this.tenderIds = agencyTenders.stream().map(Tender::getId).toList();
        this.website = tenderAgencyProfile.getWebsite();
        this.contactPerson = tenderAgencyProfile.getContactPerson();
        this.verifiedStatus = tenderAgencyProfile.getVerifiedStatus();
    }
}
