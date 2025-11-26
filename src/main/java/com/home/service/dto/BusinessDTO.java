package com.home.service.dto;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.service.Service.BusinessLocationService.BusinessLocationDTO;
import com.home.service.models.Business;
import com.home.service.models.BusinessLocation;
import com.home.service.models.SubscriptionPlan;
import com.home.service.models.User;
import com.home.service.models.enums.BusinessType;
import com.home.service.models.enums.OpeningHours;
import com.home.service.models.enums.SocialMedia;

import lombok.Data;

@Data
public class BusinessDTO {
    public Long id;
    public String name;
    public String email;
    public String phoneNumber;
    public BusinessType businessType;
    public String description;
    public List<Long> categoryIds;
    public String logo;
    public String website;
    public Integer foundedYear;
    public Integer employeeCount;
    public boolean isVerified;
    public String industry;
    public String taxId;
    public String certifications;
    public Integer minOrderQuantity;
    public String tradeTerms;
    public List<String> categories;
    public BusinessLocationDTO location;
    public SocialMedia socialMedia;
    public OpeningHours openingHours;
    public User owner;
    public List<String> images;
    public List<String> telephoneNumbers;
    public List<String> mobileNumbers;
    public boolean isFeatured;
    public SubscriptionPlan subscriptionPlan;

    public String locationJson;
    public String categoryIdsJson;
    public String openingHoursJson;
    public String socialMediaJson;

    public BusinessDTO() {
    }

    public BusinessDTO(Business business) {
        this.id = business.getId();
        this.name = business.getName();
        this.email = business.getEmail();
        this.phoneNumber = business.getPhoneNumber();
        this.businessType = business.getBusinessType();
        this.description = business.getDescription();
        this.logo = business.getLogo();
        this.website = business.getWebsite();
        this.foundedYear = business.getFoundedYear();
        this.employeeCount = business.getEmployeeCount();
        this.isVerified = business.isVerified();
        this.industry = business.getIndustry();
        this.taxId = business.getTaxId();
        this.certifications = business.getCertifications();
        this.minOrderQuantity = business.getMinOrderQuantity();
        this.tradeTerms = business.getTradeTerms();

        this.location = business.getLocation() != null ? new BusinessLocationDTO(business.getLocation()) : null;
        this.socialMedia = business.getSocialMedia();
        this.openingHours = business.getOpeningHours();

        this.images = business.getImages();
        this.telephoneNumbers = business.getTelephoneNumbers();
        this.mobileNumbers = business.getMobileNumbers();
        this.isFeatured = business.isFeatured();
        this.subscriptionPlan = business.getSubscriptionPlan();
        this.owner = business.getOwner();
    }

    public static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<Long> getCategoryIds() {
        System.out.println("Category IDs: " + categoryIdsJson);
        System.out.println(categoryIdsJson);
        if (categoryIdsJson == null || categoryIdsJson.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(categoryIdsJson, new TypeReference<List<Long>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid location format: " + e.getMessage(), e);
        }
    }

    public BusinessLocationDTO getLocationFromJSON() {
        System.out.println(locationJson);
        if (locationJson == null || locationJson.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(locationJson, BusinessLocationDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid location format: " + e.getMessage(), e);
        }
    }

    public OpeningHours getOpeningHoursFromJSON() {
        if (openingHoursJson == null || openingHoursJson.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(openingHoursJson, OpeningHours.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid opening hours format: " + e.getMessage(), e);
        }
    }

    public SocialMedia getSocialMediaFromJSON() {
        if (socialMediaJson == null || socialMediaJson.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(socialMediaJson, SocialMedia.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid social media format: " + e.getMessage(), e);
        }
    }
}
