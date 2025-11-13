package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySearchDto {
    private Long id;
    private String name;
    private String logo;
    private String industry;
    private String location;
    private String size;
    private Double rating;
    private Integer totalReviews;
    private Integer openJobs;
    private String description;
    private String founded;
}