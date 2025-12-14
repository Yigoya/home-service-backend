package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.models.Business;
import com.home.service.models.BusinessPromotion;
import com.home.service.models.enums.PromotionType;
import com.home.service.models.enums.BusinessType;
import com.home.service.repositories.BusinessPromotionRepository;
import com.home.service.repositories.BusinessRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class BusinessPromotionService {

    private final BusinessPromotionRepository businessPromotionRepository;
    private final BusinessRepository businessRepository;
    private final com.home.service.repositories.ServicesRepository servicesRepository;

    public BusinessPromotionService(BusinessPromotionRepository businessPromotionRepository,
            BusinessRepository businessRepository,
            com.home.service.repositories.ServicesRepository servicesRepository) {
        this.businessPromotionRepository = businessPromotionRepository;
        this.businessRepository = businessRepository;
        this.servicesRepository = servicesRepository;
    }

    public static class BusinessPromotionDTO {
        public Long id;
        public Long businessId;
        public Long categoryId;
        public String title;
        public String description;
        public LocalDateTime startDate;
        public LocalDateTime endDate;
        public PromotionType type;
        public Double discountPercentage;
        public boolean isFeatured;
        public Set<Long> serviceIds;
        public String imageUrl;
        public String termsAndConditions;

        public BusinessPromotionDTO() {
        }

        public BusinessPromotionDTO(BusinessPromotion promotion) {
            this.id = promotion.getId();
            this.businessId = promotion.getBusiness().getId();
            // Derive categoryId from the first attached service, if any
            this.categoryId = promotion.getServices() != null && !promotion.getServices().isEmpty()
                    ? promotion.getServices().iterator().next().getCategory() != null
                        ? promotion.getServices().iterator().next().getCategory().getId()
                        : null
                    : null;
            this.title = promotion.getTitle();
            this.description = promotion.getDescription();
            this.startDate = promotion.getStartDate();
            this.endDate = promotion.getEndDate();
            this.type = promotion.getType();
            this.discountPercentage = promotion.getDiscountPercentage();
            this.isFeatured = promotion.isFeatured();
            this.serviceIds = promotion.getServices().stream()
                    .map(service -> service.getId())
                    .collect(java.util.stream.Collectors.toSet());
            this.imageUrl = promotion.getImageUrl();
            this.termsAndConditions = promotion.getTermsAndConditions();
        }
    }

    public static class PublicPromotionDTO {
        public Long id;
        public String title;
        public String description;
        public LocalDateTime startDate;
        public LocalDateTime endDate;
        public PromotionType type;
        public Double discountPercentage;
        public String businessName;
        public String businessLogo;
        public Long businessId;
        public String businessCategory;
        public boolean isActive;
        public boolean isFeatured;
        public String imageUrl;
        public String termsAndConditions;
        public Set<ServiceInfo> services;

        public PublicPromotionDTO() {
        }

        public PublicPromotionDTO(BusinessPromotion promotion) {
            this.id = promotion.getId();
            this.title = promotion.getTitle();
            this.description = promotion.getDescription();
            this.startDate = promotion.getStartDate();
            this.endDate = promotion.getEndDate();
            this.type = promotion.getType();
            this.discountPercentage = promotion.getDiscountPercentage();
            this.businessName = promotion.getBusiness().getName();
            this.businessLogo = promotion.getBusiness().getLogo();
            this.businessId = promotion.getBusiness().getId();
            this.businessCategory = promotion.getBusiness().getIndustry();
            this.isActive = isPromotionActive(promotion);
            this.isFeatured = promotion.isFeatured();
            this.imageUrl = promotion.getImageUrl();
            this.termsAndConditions = promotion.getTermsAndConditions();
            this.services = promotion.getServices().stream()
                    .map(ServiceInfo::new)
                    .collect(Collectors.toSet());
        }

        private boolean isPromotionActive(BusinessPromotion promotion) {
            LocalDateTime now = LocalDateTime.now();
            return promotion.getStartDate().isBefore(now) && promotion.getEndDate().isAfter(now);
        }
    }

    public static class ServiceInfo {
        public Long id;
        public String name;
        public Double serviceFee;
        public String icon;

        public ServiceInfo() {
        }

        public ServiceInfo(com.home.service.models.Services service) {
            this.id = service.getId();
            // Get service name from translations or use a default
            this.name = getServiceName(service);
            this.serviceFee = service.getServiceFee();
            this.icon = service.getIcon();
        }

        private String getServiceName(com.home.service.models.Services service) {
            // Try to get name from translations, fallback to ID if no translations
            if (service.getTranslations() != null && !service.getTranslations().isEmpty()) {
                return service.getTranslations().iterator().next().getName();
            }
            return "Service " + service.getId();
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
        promotion.setFeatured(dto.isFeatured);
        promotion.setImageUrl(dto.imageUrl);
        promotion.setTermsAndConditions(dto.termsAndConditions);

        // Add services to promotion
        if (dto.serviceIds != null && !dto.serviceIds.isEmpty()) {
            Set<com.home.service.models.Services> services = new HashSet<>();
            for (Long serviceId : dto.serviceIds) {
                com.home.service.models.Services service = servicesRepository.findById(serviceId)
                        .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + serviceId));
                services.add(service);
            }
            promotion.setServices(services);
            // If categoryId not provided, infer it from the first service
            if (dto.categoryId == null && !services.isEmpty()) {
                com.home.service.models.Services first = services.iterator().next();
                if (first.getCategory() != null) {
                    dto.categoryId = first.getCategory().getId();
                }
            }
        } else if (dto.categoryId != null) {
            // When categoryId provided and serviceIds missing, attach all business services in that category
            Set<com.home.service.models.Services> servicesInCategory = business.getServices().stream()
                    .filter(s -> s.getCategory() != null && s.getCategory().getId().equals(dto.categoryId))
                    .collect(Collectors.toSet());
            promotion.setServices(servicesInCategory);
        }

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
        promotion.setFeatured(dto.isFeatured);
        promotion.setImageUrl(dto.imageUrl);
        promotion.setTermsAndConditions(dto.termsAndConditions);

        // Update services
        if (dto.serviceIds != null) {
            Set<com.home.service.models.Services> services = new HashSet<>();
            for (Long serviceId : dto.serviceIds) {
                com.home.service.models.Services service = servicesRepository.findById(serviceId)
                        .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + serviceId));
                services.add(service);
            }
            promotion.setServices(services);
        }

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

    // Public-facing methods for customers and users
    public Page<PublicPromotionDTO> getPublicActivePromotions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<BusinessPromotion> promotions = businessPromotionRepository.findByStartDateBeforeAndEndDateAfter(now, pageable);
        return promotions.map(PublicPromotionDTO::new);
    }

    public Page<PublicPromotionDTO> getPromotionsByType(PromotionType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<BusinessPromotion> promotions = businessPromotionRepository.findActivePromotionsByType(now, type, pageable);
        return promotions.map(PublicPromotionDTO::new);
    }

    public Page<PublicPromotionDTO> getPromotionsByIndustry(String industry, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<BusinessPromotion> promotions = businessPromotionRepository.findActivePromotionsByIndustry(now, industry, pageable);
        return promotions.map(PublicPromotionDTO::new);
    }

    public Page<PublicPromotionDTO> getPromotionsByBusinessType(com.home.service.models.enums.BusinessType businessType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<BusinessPromotion> promotions = businessPromotionRepository.findActivePromotionsByBusinessType(now, businessType, pageable);
        return promotions.map(PublicPromotionDTO::new);
    }

    public Page<PublicPromotionDTO> searchPromotions(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<BusinessPromotion> promotions = businessPromotionRepository.findActivePromotionsBySearch(now, searchTerm, pageable);
        return promotions.map(PublicPromotionDTO::new);
    }

    public Page<PublicPromotionDTO> getFeaturedPromotions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<BusinessPromotion> promotions = businessPromotionRepository.findFeaturedPromotions(now, pageable);
        return promotions.map(PublicPromotionDTO::new);
    }

    public PublicPromotionDTO getPublicPromotionById(Long id) {
        BusinessPromotion promotion = getPromotionById(id);
        LocalDateTime now = LocalDateTime.now();
        
        // Check if promotion is active
        if (promotion.getStartDate().isAfter(now) || promotion.getEndDate().isBefore(now)) {
            throw new EntityNotFoundException("Active promotion not found with ID: " + id);
        }
        
        return new PublicPromotionDTO(promotion);
    }

    public Page<PublicPromotionDTO> getActivePromotionsByBusiness(Long businessId, int page, int size) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));
        
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        
        // Get all promotions for the business and filter active ones
        Page<BusinessPromotion> allPromotions = businessPromotionRepository.findByBusiness(business, pageable);
        
        // Filter and convert to PublicPromotionDTO
        return allPromotions
            .map(promotion -> {
                if (promotion.getStartDate().isBefore(now) && promotion.getEndDate().isAfter(now)) {
                    return new PublicPromotionDTO(promotion);
                }
                return null;
            })
            .map(dto -> dto); // Remove null entries will be handled by Spring Data
    }

    // Method to make a promotion featured
    public BusinessPromotionDTO makePromotionFeatured(Long id, Long currentUserId) {
        BusinessPromotion promotion = getPromotionById(id);
        // if (!promotion.getBusiness().getOwner().getId().equals(currentUserId)) {
        //     throw new AccessDeniedException("Only the business owner can feature promotions");
        // }
        
        promotion.setFeatured(true);
        BusinessPromotion updatedPromotion = businessPromotionRepository.save(promotion);
        return new BusinessPromotionDTO(updatedPromotion);
    }

    // Method to remove featured status from a promotion
    public BusinessPromotionDTO removePromotionFeatured(Long id, Long currentUserId) {
        BusinessPromotion promotion = getPromotionById(id);
        // if (!promotion.getBusiness().getOwner().getId().equals(currentUserId)) {
        //     throw new AccessDeniedException("Only the business owner can unfeature promotions");
        // }
        
        promotion.setFeatured(false);
        BusinessPromotion updatedPromotion = businessPromotionRepository.save(promotion);
        return new BusinessPromotionDTO(updatedPromotion);
    }

    // Method to get single promotion details (admin/business owner view)
    public BusinessPromotionDTO getPromotionDetails(Long id, Long currentUserId) {
        BusinessPromotion promotion = getPromotionById(id);
        // if (!promotion.getBusiness().getOwner().getId().equals(currentUserId)) {
        //     throw new AccessDeniedException("Access denied to promotion details");
        // }
        return new BusinessPromotionDTO(promotion);
    }

    // Method to get services available for a business (for promotion creation)
    public Set<ServiceInfo> getBusinessServices(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));
        
        return business.getServices().stream()
                .map(ServiceInfo::new)
                .collect(Collectors.toSet());
    }

    // Method to get active promotions for businesses that offer a specific service
    public Page<PublicPromotionDTO> getPromotionsByServiceId(Long serviceId, int page, int size) {
        // First verify that the service exists
        servicesRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + serviceId));
        
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<BusinessPromotion> promotions = businessPromotionRepository.findActivePromotionsByServiceId(now, serviceId, pageable);
        return promotions.map(PublicPromotionDTO::new);
    }

    // Get active promotions by service category id
    public Page<PublicPromotionDTO> getPromotionsByCategoryId(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<BusinessPromotion> promotions = businessPromotionRepository.findActivePromotionsByCategoryId(now, categoryId, pageable);
        return promotions.map(PublicPromotionDTO::new);
    }

    // // Method to get a unique promotion for a device based on service ID
    // public PublicPromotionDTO getUniquePromotionForDevice(Long serviceId, String deviceIdentifier) {
    //     // First verify that the service exists
    //     servicesRepository.findById(serviceId)
    //             .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + serviceId));
        
    //     LocalDateTime now = LocalDateTime.now();
        
    //     // Get all active promotions for this service
    //     List<BusinessPromotion> allPromotions = businessPromotionRepository.findAllActivePromotionsByServiceId(now, serviceId);
        
    //     if (allPromotions.isEmpty()) {
    //         throw new EntityNotFoundException("No active promotions found for service ID: " + serviceId);
    //     }
        
    //     // Get promotions already shown to this device
    //     Set<Long> shownPromotionIds = getShownPromotionsForDevice(deviceIdentifier, serviceId);
        
    //     // Filter out already shown promotions
    //     List<BusinessPromotion> availablePromotions = allPromotions.stream()
    //             .filter(promotion -> !shownPromotionIds.contains(promotion.getId()))
    //             .collect(Collectors.toList());
        
    //     // If all promotions have been shown, reset and start over
    //     if (availablePromotions.isEmpty()) {
    //         clearShownPromotionsForDevice(deviceIdentifier, serviceId);
    //         availablePromotions = allPromotions;
    //     }
        
    //     // Select a random promotion from available ones
    //     BusinessPromotion selectedPromotion = availablePromotions.get(
    //         (int) (Math.random() * availablePromotions.size())
    //     );
        
    //     // Mark this promotion as shown for this device
    //     markPromotionAsShown(deviceIdentifier, serviceId, selectedPromotion.getId());
        
    //     return new PublicPromotionDTO(selectedPromotion);
    // }
    
    // Simple in-memory storage for demo - in production, use Redis or database
    private static final Map<String, Set<Long>> devicePromotionHistory = new HashMap<>();
    
    private Set<Long> getShownPromotionsForDevice(String deviceIdentifier, Long serviceId) {
        String key = deviceIdentifier + "_service_" + serviceId;
        return devicePromotionHistory.getOrDefault(key, new HashSet<>());
    }
    
    private void markPromotionAsShown(String deviceIdentifier, Long serviceId, Long promotionId) {
        String key = deviceIdentifier + "_service_" + serviceId;
        devicePromotionHistory.computeIfAbsent(key, k -> new HashSet<>()).add(promotionId);
    }
    
    private void clearShownPromotionsForDevice(String deviceIdentifier, Long serviceId) {
        String key = deviceIdentifier + "_service_" + serviceId;
        devicePromotionHistory.remove(key);
    }
}