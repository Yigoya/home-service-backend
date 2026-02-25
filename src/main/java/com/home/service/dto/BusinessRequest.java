package com.home.service.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.home.service.Service.BusinessLocationService.BusinessLocationDTO;
import com.home.service.models.enums.BusinessType;
import com.home.service.models.enums.OpeningHours;
import com.home.service.models.enums.SocialMedia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class BusinessRequest {
    @NotNull(message = "Owner ID is required")
    public Long ownerId;

    @NotBlank(message = "Name is required")
    public String name;

    public String nameAmharic;

    @NotBlank(message = "Description is required")
    public String description;

    @NotNull(message = "Category IDs are required")
    public List<Long> categoryIds;

    public String locationJson;
    public String phoneNumber;
    public String alternativeContactPhone;
    public String email;
    public String website;
    public BusinessType businessType;
    public Integer foundedYear;
    public Integer employeeCount;
    public String registrationNumber;
    @NotBlank(message = "Tax Identification Number (TIN) is required")
    public String taxId;
    public String legalRepresentativeName;
    public String primaryCategory;
    public List<String> secondaryCategories;
    public boolean localDistributionNetwork;
    public String openingHoursJson;
    public String socialMediaJson;
    public boolean isVerified;
    public boolean isFeatured;
    public MultipartFile[] images;

    public String serviceIdsJson;

    public List<Long> getServiceIds() {
        if (serviceIdsJson == null || serviceIdsJson.isEmpty()) {
            return null;
        }
        try {
            log.debug("Parsing service IDs JSON: {}", serviceIdsJson);
            return mapper.readValue(serviceIdsJson, mapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (JsonProcessingException e) {
            log.error("Error parsing service IDs JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid service IDs format: " + e.getMessage(), e);
        }
    }

    public static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public BusinessLocationDTO getLocation() {
        System.out.println(locationJson);
        if (locationJson == null || locationJson.isEmpty()) {
            return null;
        }
        try {
            log.debug("Parsing location JSON: {}", locationJson);
            return mapper.readValue(locationJson, BusinessLocationDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing location JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid location format: " + e.getMessage(), e);
        }
    }

    public OpeningHours getOpeningHours() {
        if (openingHoursJson == null || openingHoursJson.isEmpty()) {
            return null;
        }
        try {
            log.debug("Parsing opening hours JSON: {}", openingHoursJson);
            return mapper.readValue(openingHoursJson, OpeningHours.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing opening hours JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid opening hours format: " + e.getMessage(), e);
        }
    }

    public SocialMedia getSocialMedia() {
        if (socialMediaJson == null || socialMediaJson.isEmpty()) {
            return null;
        }
        try {
            log.debug("Parsing social media JSON: {}", socialMediaJson);
            return mapper.readValue(socialMediaJson, SocialMedia.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing social media JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid social media format: " + e.getMessage(), e);
        }
    }
}