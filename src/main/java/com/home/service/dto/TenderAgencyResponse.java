package com.home.service.dto;

import lombok.Data;

@Data
public class TenderAgencyResponse {
    private Long id;
    private String companyName;
    private String tinNumber;
    private String website;
    private String contactPerson;
    private String verifiedStatus;
    private String businessLicensePath;

    public TenderAgencyResponse(Long id, String companyName, String tinNumber, String website, String contactPerson,
            String verifiedStatus) {
        this.id = id;
        this.companyName = companyName;
        this.tinNumber = tinNumber;
        this.website = website;
        this.contactPerson = contactPerson;
        this.verifiedStatus = verifiedStatus;
    }

    public TenderAgencyResponse(Long id, String companyName, String tinNumber, String website, String contactPerson,
            String verifiedStatus, String businessLicensePath) {
        this.id = id;
        this.companyName = companyName;
        this.tinNumber = tinNumber;
        this.website = website;
        this.contactPerson = contactPerson;
        this.verifiedStatus = verifiedStatus;
        this.businessLicensePath = businessLicensePath;
    }

}
