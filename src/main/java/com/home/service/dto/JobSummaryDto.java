package com.home.service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

// For job search results list
@Data
public class JobSummaryDto {
    private Long id;
    private String title;
    private String description;
    private String companyName;
    private String companyLocation;
    private String jobLocation;
    private String jobType;
    private Instant postedDate;
    private String companyLogo;
    private String category;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private String level;
    private LocalDate applicationDeadline;
    private String contactEmail;
    private String contactPhone;
    private Instant savedDate;
}