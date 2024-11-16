package com.home.service.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianProofResponse {
    private Long technicianId;
    private String technicianName;
    private String technicianEmail;
    private String documentUrl;
    private String documentType;
    private LocalDateTime uploadedAt;
}
