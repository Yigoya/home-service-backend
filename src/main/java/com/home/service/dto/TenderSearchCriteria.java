package com.home.service.dto;

import java.time.LocalDateTime;

import com.home.service.models.enums.TenderStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenderSearchCriteria {
    private String keyword;
    private TenderStatus status;
    private String location;
    private Long serviceId;
    private LocalDateTime datePosted;
    private LocalDateTime closingDate;
    private int page;
    private int size;
}