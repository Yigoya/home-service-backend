package com.home.service.dto;

import lombok.Data;

@Data
public class InquiryAnalyticsDTO {
    private Long totalInquiries;
    private Long pendingInquiries;
    private Long respondedInquiries;
    private Long closedInquiries;
}