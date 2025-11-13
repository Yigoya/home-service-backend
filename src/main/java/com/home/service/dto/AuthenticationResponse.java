package com.home.service.dto;

import com.home.service.models.enums.EthiopianLanguage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationResponse {
    private final String token;
    private final UserResponse user;
    private TechnicianResponse technician;
    private CustomerResponse customer;
    private OperatorResponse operator;
    private TenderAgencyProfileResponse tenderAgencyProfile;
    
    // Profile IDs
    private Long technicianId;
    private Long customerId;
    private Long operatorId;
    private Long agencyId;
    private Long companyId;
    private Long jobSeekerId;

    public AuthenticationResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

}
