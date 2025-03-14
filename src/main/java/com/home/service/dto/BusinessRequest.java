package com.home.service.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.home.service.Service.BusinessLocationService.BusinessLocationDTO;
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
    private Long ownerId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category IDs are required")
    private List<Long> categoryIds;

    private String locationJson;
    private String phoneNumber;
    private String email;
    private String website;
    private String openingHoursJson;
    private String socialMediaJson;
    private boolean isVerified;
    private boolean isFeatured;
    private MultipartFile[] images;

    private static final ObjectMapper mapper = new ObjectMapper()
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