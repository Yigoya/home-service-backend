package com.home.service.dto;

import lombok.Data;

@Data
public class ApplicationRequestDto {
    private Long userId;
    private String coverLetter;
    private String resumeUrl; 
}
