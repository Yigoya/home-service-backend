package com.home.service.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.home.service.models.AgencyProfile;
import com.home.service.models.enums.EthiopianLanguage;

public class AgencyProfileDTO {

    private Long id;
    private Long userId;
    private String businessName;
    private String description;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;
    private String website;
    private String document;
    private Set<ServiceDTO> services;

    public AgencyProfileDTO(AgencyProfile agencyProfile) {
        this.id = agencyProfile.getId();
        this.userId = agencyProfile.getUser().getId();
        this.businessName = agencyProfile.getBusinessName();
        this.description = agencyProfile.getDescription();
        this.address = agencyProfile.getAddress();
        this.city = agencyProfile.getCity();
        this.state = agencyProfile.getState();
        this.zip = agencyProfile.getZip();
        this.country = agencyProfile.getCountry();
        this.phone = agencyProfile.getPhone();
        this.website = agencyProfile.getWebsite();
        this.document = agencyProfile.getDocument();
        this.services = agencyProfile.getServices().stream()
                .map(service -> new ServiceDTO(service, EthiopianLanguage.ENGLISH))
                .collect(Collectors.toSet());
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public Set<ServiceDTO> getServices() {
        return services;
    }

    public void setServices(Set<ServiceDTO> services) {
        this.services = services;
    }
}