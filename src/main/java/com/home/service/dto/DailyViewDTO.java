package com.home.service.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DailyViewDTO {
    private LocalDateTime date;
    private Long views;
}
