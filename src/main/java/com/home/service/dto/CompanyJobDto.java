package com.home.service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class CompanyJobDto {
    private Long id;
    private String title;
    private String description;
    private String jobLocation;
    private String jobType;
    private String category;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private String level;
    private Instant postedDate;
    private LocalDate applicationDeadline;
    private String contactEmail;
    private String contactPhone;
    private List<String> tags;
    private List<String> responsibilities;
    private List<String> benefits;
    private String status; // ACTIVE, EXPIRED, DRAFT
    private Long applicationCount;
}