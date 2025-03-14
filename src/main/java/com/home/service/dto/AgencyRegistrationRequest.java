package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AgencyRegistrationRequest {
    private String email;
    private String password;
    private String contactPerson;
    private String phoneNumber;
    private String companyName;
    private String tinNumber;

}
