package com.home.service.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.home.service.models.BusinessServices.ServiceOption;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class BusinessServiceRequest {
    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @Positive(message = "Price must be positive")
    private double price;

    private boolean available;
    private String serviceOptionsJson;
    private MultipartFile image;

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<ServiceOption> getServiceOptions() {
        if (serviceOptionsJson == null || serviceOptionsJson.isEmpty()) {
            return null;
        }
        try {
            log.debug("Parsing service options JSON: {}", serviceOptionsJson);
            List<ServiceOptionDTO> dtos = mapper.readValue(serviceOptionsJson,
                    new TypeReference<List<ServiceOptionDTO>>() {
                    });
            return dtos.stream().map(dto -> {
                ServiceOption option = new ServiceOption();
                option.setName(dto.getName());
                option.setDescription(dto.getDescription());
                option.setPrices(List.of(dto.getPrice()));
                return option;
            }).collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.error("Error parsing service options JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid service options format: " + e.getMessage(), e);
        }
    }
}