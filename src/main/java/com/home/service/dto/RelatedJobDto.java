package com.home.service.dto;

import lombok.Data;

@Data
public class RelatedJobDto {
    private Long id;
    private String title;
    private String company;
    private String location;
    private String type;
    private String salary;
    private String logo;
    private String posted;
    private String level;
}