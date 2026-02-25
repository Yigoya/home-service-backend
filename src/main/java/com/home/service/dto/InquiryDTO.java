package com.home.service.dto;

import java.time.LocalDateTime;

import com.home.service.models.enums.InquiryStatus;

import lombok.Data;

@Data
public class InquiryDTO {
    private Long id;
    private String subject;
    private String message;
    private Long senderId;
    private Long recipientId;
    private Long productId;
    private InquiryStatus status;
    private LocalDateTime respondedAt;
    // Fallback contact info when senderId is not provided
    private String name;
    private String email;
    private String phone;
}
