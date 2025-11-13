package com.home.service.dto;

import lombok.Data;
import java.util.List;

@Data
public class CompanyDataDto {
    private String name;
    private String logo;
    private String description;
    private String industry;
    private String size;
    private String founded;
    private String location;
    private String website;
    private Double rating;
    private Integer totalReviews;
    private Integer openJobs;
    private List<String> benefits;
    private List<String> culture;
}