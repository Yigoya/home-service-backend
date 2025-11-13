package com.home.service.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.SavedContentService;
import com.home.service.dto.SavedBusinessSummaryDto;
import com.home.service.models.SavedBusiness;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final SavedContentService savedContentService;

    public CustomerController(SavedContentService savedContentService) {
        this.savedContentService = savedContentService;
    }

    // Saved Business Management
    @CrossOrigin(originPatterns = "*")
    @PostMapping("/{customerId}/saved-businesses/{businessId}")
    public ResponseEntity<String> saveBusiness(@PathVariable Long customerId, @PathVariable Long businessId) {
        savedContentService.saveBusiness(businessId, customerId);
        return ResponseEntity.ok("Business saved successfully");
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/{customerId}/saved-businesses/{businessId}")
    public ResponseEntity<Void> unsaveBusiness(@PathVariable Long customerId, @PathVariable Long businessId) {
        savedContentService.unsaveBusiness(businessId, customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{customerId}/saved-businesses")
    public ResponseEntity<Page<SavedBusinessSummaryDto>> getSavedBusinesses(@PathVariable Long customerId, Pageable pageable) {
        Page<SavedBusiness> savedBusinesses = savedContentService.getSavedBusinesses(customerId, pageable);
        Page<SavedBusinessSummaryDto> dtos = savedBusinesses.map(SavedBusinessSummaryDto::new);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{customerId}/saved-businesses/{businessId}/is-saved")
    public ResponseEntity<Boolean> isBusinessSaved(@PathVariable Long customerId, @PathVariable Long businessId) {
        boolean isSaved = savedContentService.isBusinessSaved(businessId, customerId);
        return ResponseEntity.ok(isSaved);
    }
}