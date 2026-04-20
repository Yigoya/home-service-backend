package com.home.service.dto.fayda;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FaydaVerifyTechnicianResponse {
    private boolean verified;
    private boolean nationalIdMatched;
    private String nationalId;
    private String name;
    private String subject;
    private String verificationToken;
}
