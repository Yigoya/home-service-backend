package com.home.service.dto;

import com.home.service.models.enums.InquiryStatus;

import lombok.Data;

@Data
public class InquiryResponseDTO {
    private String responseMessage;
    private InquiryStatus status;
}
