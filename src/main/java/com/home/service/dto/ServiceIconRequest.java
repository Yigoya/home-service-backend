package com.home.service.dto;

import org.springframework.web.multipart.MultipartFile;

public class ServiceIconRequest {
    private Long serviceId;
    private MultipartFile iconFile;

    // Getters and setters
    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public MultipartFile getIconFile() {
        return iconFile;
    }

    public void setIconFile(MultipartFile iconFile) {
        this.iconFile = iconFile;
    }
}