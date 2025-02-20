package com.home.service.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyProfileRequest {
    private String name;
    private String phoneNumber;
    private String email;
    private String password;

    private String businessName;
    private String description;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String companyPhoneNumber;
    private String website;
    private MultipartFile document;
    private MultipartFile image;
    private Long serviceId;

}