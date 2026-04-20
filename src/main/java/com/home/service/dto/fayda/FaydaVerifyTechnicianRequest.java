package com.home.service.dto.fayda;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaydaVerifyTechnicianRequest {
    @NotBlank(message = "Authorization code is required")
    private String code;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "National ID is required")
    private String nationalId;
}
