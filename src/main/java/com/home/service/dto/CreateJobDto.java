package com.home.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class CreateJobDto {
    private String title;
    private String description;
    private String jobLocation;
    private String jobType;
    private Long companyId;
    private Long serviceId;
    private List<String> responsibilities;
    private List<String> qualifications;
    private List<String> benefits;
    private List<String> tags;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private String level;
    private LocalDate applicationDeadline;
    private String contactEmail;
    private String contactPhone;
}