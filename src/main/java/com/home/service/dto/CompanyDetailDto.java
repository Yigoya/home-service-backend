package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDetailDto {
    private String name;
    private String logo;
    private String coverImage;
    private String industry;
    private String size;
    private String founded;
    private String location;
    private String website;
    private String email;
    private String phone;
    private Double rating;
    private Integer totalReviews;
    private Integer openJobs;
    private Integer totalHires;
    private String description;
    private List<String> benefits;
    private List<String> culture;
    private List<RecruitingHistoryDto> recruitingHistory;
    private List<CurrentOpeningDto> currentOpenings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruitingHistoryDto {
        private String year;
        private Integer totalHires;
        private List<DepartmentHireDto> departments;
        private List<String> highlights;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentHireDto {
        private String name;
        private Integer hires;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentOpeningDto {
        private Long id;
        private String title;
        private String department;
        private String type;
        private String posted;
        private Integer applicants;
    }
}