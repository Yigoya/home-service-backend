package com.home.service.dto;

import com.home.service.models.enums.EthiopianLanguage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCatagoryRequest {
    private String name;
    private String description;
    private EthiopianLanguage lang;
}
