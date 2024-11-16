package com.home.service.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadPaymentProofRequest {
    @NotNull(message = "File cannot be null")
    private MultipartFile file;

    @NotNull(message = "Technician ID cannot be null")
    private Long technicianId;
}
