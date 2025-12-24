package com.home.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import com.home.service.Service.BusinessPromotionService;
import com.home.service.Service.BusinessPromotionService.BusinessPromotionDTO;
import com.home.service.Service.BusinessPromotionService.PublicPromotionDTO;
import com.home.service.dto.PromotionRequest;
import com.home.service.models.BusinessPromotion;
import com.home.service.models.enums.PromotionType;
import com.home.service.services.FileStorageService;

@RestController
@RequestMapping
public class PromotionController {

    private final BusinessPromotionService businessPromotionService;
    private final FileStorageService fileStorageService;

    public PromotionController(BusinessPromotionService businessPromotionService,
                               FileStorageService fileStorageService) {
        this.businessPromotionService = businessPromotionService;
        this.fileStorageService = fileStorageService;
    }

    // Management (Business/Admin)
    @PostMapping(value = "/promotions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BusinessPromotionDTO> createPromotion(@ModelAttribute PromotionRequest request) {
        Long currentUserId = 4L; // TODO replace with authenticated user id

        BusinessPromotionDTO dto = new BusinessPromotionDTO();
        dto.businessId = request.getBusinessId();
        dto.categoryId = request.getCategoryId();
        dto.title = request.getTitle();
        dto.description = request.getDescription();
        dto.startDate = request.getStartDate();
        dto.endDate = request.getEndDate();
        dto.type = request.getType();
        dto.discountPercentage = request.getDiscountPercentage();
        dto.isFeatured = request.getIsFeatured() != null ? request.getIsFeatured() : false;
        dto.serviceIds = request.getServiceIds();
        dto.termsAndConditions = request.getTermsAndConditions();

        if (request.getImages() != null && request.getImages().length > 0) {
            try {
                java.util.List<String> imagePaths = new java.util.ArrayList<>();
                for (org.springframework.web.multipart.MultipartFile file : request.getImages()) {
                    if (file != null && !file.isEmpty()) {
                        String storedFileName = fileStorageService.storeFile(file);
                        imagePaths.add(storedFileName);
                    }
                }
                dto.images = imagePaths;
            } catch (RuntimeException ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        BusinessPromotionDTO promotion = businessPromotionService.createPromotion(dto, currentUserId);
        return new ResponseEntity<>(promotion, HttpStatus.CREATED);
    }

    @GetMapping("/promotions/{id}")
    public ResponseEntity<BusinessPromotionDTO> getPromotionById(@PathVariable Long id) {
        BusinessPromotion promotion = businessPromotionService.getPromotionById(id);
        return ResponseEntity.ok(new BusinessPromotionDTO(promotion));
    }

    @PutMapping("/promotions/{id}")
    public ResponseEntity<BusinessPromotionDTO> updatePromotion(@PathVariable Long id,
            @RequestBody BusinessPromotionDTO promotionDTO) {
        Long currentUserId = 4L;
        BusinessPromotionDTO promotion = businessPromotionService.updatePromotion(id, promotionDTO, currentUserId);
        return ResponseEntity.ok(promotion);
    }

    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        Long currentUserId = 4L;
        businessPromotionService.deletePromotion(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/promotions/business/{businessId}")
    public ResponseEntity<Page<BusinessPromotionDTO>> getPromotionsByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessPromotionDTO> promotions = businessPromotionService.getPromotionsByBusiness(businessId, page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/active")
    public ResponseEntity<Page<BusinessPromotionDTO>> getActivePromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessPromotionDTO> promotions = businessPromotionService.getActivePromotions(page, size);
        return ResponseEntity.ok(promotions);
    }

    @PatchMapping("/promotions/{id}/featured")
    public ResponseEntity<BusinessPromotionDTO> makePromotionFeatured(@PathVariable Long id) {
        Long currentUserId = 4L;
        BusinessPromotionDTO promotion = businessPromotionService.makePromotionFeatured(id, currentUserId);
        return ResponseEntity.ok(promotion);
    }

    @PatchMapping("/promotions/{id}/unfeatured")
    public ResponseEntity<BusinessPromotionDTO> removePromotionFeatured(@PathVariable Long id) {
        Long currentUserId = 4L;
        BusinessPromotionDTO promotion = businessPromotionService.removePromotionFeatured(id, currentUserId);
        return ResponseEntity.ok(promotion);
    }

    @GetMapping("/promotions/{id}/details")
    public ResponseEntity<BusinessPromotionDTO> getPromotionDetails(@PathVariable Long id) {
        Long currentUserId = 4L;
        BusinessPromotionDTO promotion = businessPromotionService.getPromotionDetails(id, currentUserId);
        return ResponseEntity.ok(promotion);
    }

    // Public (Customer-facing)
    @GetMapping("/promotions/public")
    public ResponseEntity<Page<PublicPromotionDTO>> getPublicPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<PublicPromotionDTO> promotions = businessPromotionService.getPublicActivePromotions(page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/public/{id}")
    public ResponseEntity<PublicPromotionDTO> getPublicPromotionById(@PathVariable Long id) {
        PublicPromotionDTO promotion = businessPromotionService.getPublicPromotionById(id);
        return ResponseEntity.ok(promotion);
    }

    @GetMapping("/promotions/public/featured")
    public ResponseEntity<Page<PublicPromotionDTO>> getFeaturedPublicPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Page<PublicPromotionDTO> promotions = businessPromotionService.getFeaturedPromotions(page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/public/type/{type}")
    public ResponseEntity<Page<PublicPromotionDTO>> getPromotionsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            PromotionType promotionType = PromotionType.valueOf(type.toUpperCase());
            Page<PublicPromotionDTO> promotions = businessPromotionService.getPromotionsByType(promotionType, page, size);
            return ResponseEntity.ok(promotions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/promotions/public/industry/{industry}")
    public ResponseEntity<Page<PublicPromotionDTO>> getPromotionsByIndustry(
            @PathVariable String industry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<PublicPromotionDTO> promotions = businessPromotionService.getPromotionsByIndustry(industry, page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/public/business-type/{businessType}")
    public ResponseEntity<Page<PublicPromotionDTO>> getPromotionsByBusinessType(
            @PathVariable String businessType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            com.home.service.models.enums.BusinessType type = com.home.service.models.enums.BusinessType.valueOf(businessType.toUpperCase());
            Page<PublicPromotionDTO> promotions = businessPromotionService.getPromotionsByBusinessType(type, page, size);
            return ResponseEntity.ok(promotions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/promotions/public/search")
    public ResponseEntity<Page<PublicPromotionDTO>> searchPublicPromotions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Page<PublicPromotionDTO> promotions = businessPromotionService.searchPromotions(query.trim(), page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/public/business/{businessId}")
    public ResponseEntity<Page<PublicPromotionDTO>> getPublicPromotionsByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PublicPromotionDTO> promotions = businessPromotionService.getActivePromotionsByBusiness(businessId, page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/public/service/{serviceId}")
    public ResponseEntity<Page<PublicPromotionDTO>> getPromotionsByServiceId(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PublicPromotionDTO> promotions = businessPromotionService.getPromotionsByServiceId(serviceId, page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/public/category/{categoryId}")
    public ResponseEntity<Page<PublicPromotionDTO>> getPromotionsByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<PublicPromotionDTO> promotions = businessPromotionService.getPromotionsByCategoryId(categoryId, page, size);
        return ResponseEntity.ok(promotions);
    }
}
