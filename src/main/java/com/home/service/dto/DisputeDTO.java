package com.home.service.dto;

import java.time.LocalDateTime;

import com.home.service.models.enums.DisputeStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisputeDTO {

    private Long id;
    private String personName;
    private String description;
    private String reason;
    private DisputeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Getters and setters...
}
