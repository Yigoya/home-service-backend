package com.home.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.Service.FaydaService;
import com.home.service.dto.fayda.FaydaAuthorizationUrlResponse;
import com.home.service.dto.fayda.FaydaVerifyTechnicianRequest;
import com.home.service.dto.fayda.FaydaVerifyTechnicianResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/fayda")
@RequiredArgsConstructor
public class FaydaAuthController {

    private final FaydaService faydaService;

    @GetMapping("/authorize")
    public ResponseEntity<FaydaAuthorizationUrlResponse> getAuthorizationUrl() {
        return ResponseEntity.ok(faydaService.createAuthorizationUrl());
    }

    @PostMapping("/verify-technician")
    public ResponseEntity<FaydaVerifyTechnicianResponse> verifyTechnician(
            @Valid @RequestBody FaydaVerifyTechnicianRequest request) {
        return ResponseEntity.ok(faydaService.verifyTechnicianAndIssueToken(request));
    }
}
