package com.home.service.dto;

import lombok.Data;

@Data
public class ServiceImportDTO {
    private int level; // 0 for category, >0 for service
    private String nameEnglish;
    private String nameAmharic;
    private String nameOromo;
    private String descriptionEnglish;
    private String descriptionAmharic;
    private String descriptionOromo;
    private String iconFileName; // New field for icon filename

    // Add getter and setter for iconFileName
    public String getIconFileName() {
        return iconFileName;
    }

    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }
}