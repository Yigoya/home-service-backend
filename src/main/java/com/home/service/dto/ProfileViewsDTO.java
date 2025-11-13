package com.home.service.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProfileViewsDTO {
    private Long totalViews;
    private List<DailyViewDTO> dailyViews;
}
