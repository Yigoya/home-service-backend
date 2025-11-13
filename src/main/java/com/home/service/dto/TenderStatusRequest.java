package com.home.service.dto;

import com.home.service.models.enums.TenderStatus;

public class TenderStatusRequest {
    private TenderStatus status;

    public TenderStatus getStatus() {
        return status;
    }

    public void setStatus(TenderStatus status) {
        this.status = status;
    }
}
