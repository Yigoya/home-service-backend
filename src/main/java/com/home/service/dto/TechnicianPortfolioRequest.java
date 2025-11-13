package com.home.service.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class TechnicianPortfolioRequest {
    private String description;

    // Preferred keys
    private MultipartFile beforeImage;
    private MultipartFile afterImage;

    // Alternate keys some clients might use
    private MultipartFile before;
    private MultipartFile after;
}
