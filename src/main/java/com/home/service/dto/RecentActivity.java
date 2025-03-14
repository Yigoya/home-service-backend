package com.home.service.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RecentActivity {
    private String action;
    private LocalDateTime timestamp;
    private String details;
}