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

    public AuthenticationResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

}
