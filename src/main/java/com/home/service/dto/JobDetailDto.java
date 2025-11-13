package com.home.service.dto;

import java.util.List;

import lombok.Data;

@Data
public class JobDetailDto extends JobSummaryDto {
    private String description;
    private List<String> responsibilities;
    private List<String> qualifications;
    private List<String> benefits;
    private List<String> tags;
    private CompanyDataDto companyData;
    private List<RelatedJobDto> relatedJobs;
}