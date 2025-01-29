package com.home.service.dto;

import org.springframework.web.multipart.MultipartFile;

import com.home.service.models.enums.EthiopianLanguage;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCatagoryRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private Boolean isMobileCategory = false;

    @NotBlank(message = "Description is required")
    private String description;

    private EthiopianLanguage lang;

    private MultipartFile icon;
}
