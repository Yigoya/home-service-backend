package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.models.Business;
import com.home.service.models.BusinessPromotion;
import com.home.service.models.enums.PromotionType;
import com.home.service.repositories.BusinessPromotionRepository;
import com.home.service.repositories.BusinessRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.time.LocalDateTime;

@Service
public class BusinessPromotionService {

    private final BusinessPromotionRepository businessPromotionRepository;
    private final BusinessRepository businessRepository;

    public BusinessPromotionService(BusinessPromotionRepository businessPromotionRepository,
            BusinessRepository businessRepository) {
        this.businessPromotionRepository = businessPromotionRepository;
        this.businessRepository = businessRepository;
    }

    public static class BusinessPromotionDTO {
        public Long id;
        public Long businessId;
        public String title;
        public String description;
        public LocalDateTime startDate;
        public LocalDateTime endDate;
        public PromotionType type;
        public Double discountPercentage;

        public BusinessPromotionDTO() {
        }

        public BusinessPromotionDTO(BusinessPromotion promotion) {
            this.id = promotion.getId();
            this.businessId = promotion.getBusiness().getId();
            this.title = promotion.getTitle();
            this.description = promotion.getDescription();
            this.startDate = promotion.getStartDate();
            this.endDate = promotion.getEndDate();
            this.type = promotion.getType();
            this.discountPercentage = promotion.getDiscountPercentage();
        }
    }

    public BusinessPromotionDTO createPromotion(@Valid BusinessPromotionDTO dto, Long currentUserId) {
        Business business = businessRepository.findById(dto.businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + dto.businessId));
        // if (!business.getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("Only the business owner can create
        // promotions");
        // }

        BusinessPromotion promotion = new BusinessPromotion();
        promotion.setBusiness(business);
        promotion.setTitle(dto.title);
        promotion.setDescription(dto.description);
        promotion.setStartDate(dto.startDate);
        promotion.setEndDate(dto.endDate);
        promotion.setType(dto.type);
        promotion.setDiscountPercentage(dto.discountPercentage);

        BusinessPromotion savedPromotion = businessPromotionRepository.save(promotion);
        return new BusinessPromotionDTO(savedPromotion);
    }

    public BusinessPromotion getPromotionById(Long id) {
        return businessPromotionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found with ID: " + id));
    }

    public BusinessPromotionDTO updatePromotion(Long id, @Valid BusinessPromotionDTO dto, Long currentUserId) {
        BusinessPromotion promotion = getPromotionById(id);
        // if (!promotion.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("Only the business owner can update
        // promotions");
        // }

        Business business = businessRepository.findById(dto.businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + dto.businessId));
        promotion.setBusiness(business);
        promotion.setTitle(dto.title);
        promotion.setDescription(dto.description);
        promotion.setStartDate(dto.startDate);
        promotion.setEndDate(dto.endDate);
        promotion.setType(dto.type);
        promotion.setDiscountPercentage(dto.discountPercentage);

        BusinessPromotion updatedPromotion = businessPromotionRepository.save(promotion);
        return new BusinessPromotionDTO(updatedPromotion);
    }

    public void deletePromotion(Long id, Long currentUserId) {
        BusinessPromotion promotion = getPromotionById(id);
        // if (!promotion.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("Only the business owner can delete
        // promotions");
        // }
        businessPromotionRepository.delete(promotion);
    }

    public Page<BusinessPromotionDTO> getPromotionsByBusiness(Long businessId, int page, int size) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));
        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessPromotion> promotions = businessPromotionRepository.findByBusiness(business, pageable);
        return promotions.map(BusinessPromotionDTO::new);
    }

    public Page<BusinessPromotionDTO> getActivePromotions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<BusinessPromotion> promotions = businessPromotionRepository.findByStartDateBeforeAndEndDateAfter(now,
                pageable);
        return promotions.map(BusinessPromotionDTO::new);
    }
}