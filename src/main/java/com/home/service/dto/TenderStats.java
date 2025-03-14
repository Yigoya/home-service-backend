package com.home.service.dto;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.home.service.models.enums.TenderStatus;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TenderStats {
    private LocalDate date;
    private Long tenderCount;
    private TenderStatus status;

    public TenderStats(LocalDate date, Long tenderCount, TenderStatus status) {
        this.date = date;
        this.tenderCount = tenderCount;
        this.status = status;
    }

    // Getters (required for sorting and potential serialization)
    public LocalDate getDate() {
        return date;
    }

    public Long getTenderCount() {
        return tenderCount;
    }

    public TenderStatus getStatus() {
        return status;
    }
}