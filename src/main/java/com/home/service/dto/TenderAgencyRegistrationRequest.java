package com.home.service.dto;

import lombok.Data;

@Data
public class TenderAgencyRegistrationRequest {
    private String companyName;
    private String tinNumber;
    private String website;
    private String contactPerson;
    private String email;
    private String password;

}
