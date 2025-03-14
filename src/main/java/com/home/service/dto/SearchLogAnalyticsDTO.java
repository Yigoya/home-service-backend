package com.home.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchLogAnalyticsDTO {
    private String query;
    private long count;

    public SearchLogAnalyticsDTO(String query, long count) {
        this.query = query;
        this.count = count;
    }

    // Getters and setters
}