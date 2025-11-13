package com.home.service.dto;

import java.util.Set;

import lombok.Data;

@Data
public class CreateJobSeekerProfileRequest {
    private Long userId;
    private String headline;
    private String summary;
    private Set<String> skills;
}