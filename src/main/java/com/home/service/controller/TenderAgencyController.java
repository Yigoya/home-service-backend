package com.home.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.TenderAgencyService;
import com.home.service.Service.TenderService;
import com.home.service.dto.AgencyStatisticsResponse;
import com.home.service.dto.TenderAgencyRegistrationRequest;
import com.home.service.dto.AuthenticationResponse;
import com.home.service.dto.TenderAgencyUpdateRequest;
import com.home.service.dto.TenderAgencyResponse;
import com.home.service.dto.TenderCreationRequest;
import com.home.service.dto.TenderResponse;
import com.home.service.dto.TenderStatusRequest;
import com.home.service.dto.TenderUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tender-agencies")
@RequiredArgsConstructor
public class TenderAgencyController {

    private final TenderAgencyService tenderAgencyService;
    private final TenderService tenderService;

    // 1. Registration Endpoint
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuthenticationResponse> registerAgency(
            @Valid @RequestBody TenderAgencyRegistrationRequest request) {
        return ResponseEntity.ok(tenderAgencyService.registerAgency(request));
    }

    // 2. Get Agency Profile
    @GetMapping("/{agencyId}/profile")
    public ResponseEntity<TenderAgencyResponse> getAgencyProfile(
            @PathVariable Long agencyId) {
        return ResponseEntity.ok(tenderAgencyService.getAgencyProfile(agencyId));
    }

    // 2.1. Get Agency Profile by User ID
    @GetMapping("/user/{userId}/profile")
    public ResponseEntity<TenderAgencyResponse> getAgencyProfileByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(tenderAgencyService.getAgencyProfileByUserId(userId));
    }

    // 3. Update Agency Profile
    @CrossOrigin(originPatterns = "*")
    @PutMapping("/{agencyId}/profile")
    public ResponseEntity<TenderAgencyResponse> updateAgencyProfile(
            @PathVariable Long agencyId,
            @Valid @RequestBody TenderAgencyUpdateRequest request) {
        return ResponseEntity.ok(tenderAgencyService.updateAgencyProfile(agencyId, request));
    }

    // 4. Upload Business License
    @PostMapping(value = "/{agencyId}/license", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadBusinessLicense(
            @PathVariable Long agencyId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(tenderAgencyService.uploadBusinessLicense(agencyId, file));
    }

    // 5. Create New Tender
    @PostMapping("/{agencyId}/tenders")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TenderResponse> createTender(
            @PathVariable Long agencyId,
            @Valid @RequestBody TenderCreationRequest request) {
        return ResponseEntity.ok(tenderService.addAgencyTender(request, agencyId));
    }

    // 6. Get All Agency Tenders
    @GetMapping("/{agencyId}/tenders")
    public ResponseEntity<List<TenderResponse>> getAgencyTenders(
            @PathVariable Long agencyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePosted,desc") String sort) {
        return ResponseEntity.ok(tenderService.getAgencyTenders(agencyId, page, size, sort));
    }

    // 7. Get Single Tender
    @GetMapping("/{agencyId}/tenders/{tenderId}")
    public ResponseEntity<TenderResponse> getTender(
            @PathVariable Long agencyId,
            @PathVariable Long tenderId) {
        return ResponseEntity.ok(tenderService.getTender(agencyId, tenderId));
    }

    // 8. Update Tender
    @CrossOrigin(originPatterns = "*")
    @PutMapping("/{agencyId}/tenders/{tenderId}")
    public ResponseEntity<TenderResponse> updateTender(
            @PathVariable Long agencyId,
            @PathVariable Long tenderId,
            @Valid @RequestBody TenderUpdateRequest request) {
        return ResponseEntity.ok(tenderService.updateTender(agencyId, tenderId, request));
    }

    // 9. Upload Tender Document
    @PostMapping(value = "/{agencyId}/tenders/{tenderId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadTenderDocument(
            @PathVariable Long agencyId,
            @PathVariable Long tenderId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(tenderService.uploadTenderDocument(agencyId, tenderId, file));
    }

    // 10. Change Tender Status
    @PatchMapping("/{agencyId}/tenders/{tenderId}/status")
    public ResponseEntity<TenderResponse> changeTenderStatus(
            @PathVariable Long agencyId,
            @PathVariable Long tenderId,
            @RequestBody TenderStatusRequest statusRequest) {
        return ResponseEntity.ok(tenderService.updateTenderStatus(agencyId, tenderId, statusRequest));
    }

    // 11. Get Agency Statistics
    @GetMapping("/{agencyId}/statistics")
    public ResponseEntity<AgencyStatisticsResponse> getAgencyStatistics(
            @PathVariable Long agencyId) {
        return ResponseEntity.ok(tenderAgencyService.getAgencyStatistics(agencyId));
    }

    // 12. Delete Tender
    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/{agencyId}/tenders/{tenderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTender(
            @PathVariable Long agencyId,
            @PathVariable Long tenderId) {
        tenderService.deleteTender(agencyId, tenderId);
    }
}
