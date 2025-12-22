package com.home.service.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.home.service.Service.BusinessLocationService.BusinessLocationDTO;
import com.home.service.config.exceptions.BadRequestException;
import com.home.service.dto.BusinessDTO;
import com.home.service.dto.BusinessRequest;
import com.home.service.dto.DailyViewDTO;
import com.home.service.dto.ProfileViewsDTO;
import com.home.service.models.Business;
import com.home.service.models.BusinessLocation;
import com.home.service.models.BusinessReview;
import com.home.service.models.BusinessServices;
import com.home.service.models.Services;
import com.home.service.models.SubscriptionPlan;
import com.home.service.models.User;
import com.home.service.models.enums.BusinessType;
import com.home.service.models.enums.OpeningHours;
import com.home.service.models.enums.PlanType;
import com.home.service.models.enums.SocialMedia;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.BusinessLocationRepository;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.BusinessReviewRepository;
import com.home.service.repositories.BusinessServiceRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);

    private final BusinessRepository businessRepository;
    private final ServiceRepository servicesRepository; // For category validation
    private final UserRepository userRepository;
    private final BusinessServiceRepository businessServiceRepository;
    private final BusinessReviewRepository businessReviewRepository;
    private final BusinessLocationRepository businessLocationRepository;
    private final FileStorageService fileStorageService;
    private final SubscriptionService subscriptionService;

    public BusinessService(BusinessRepository businessRepository, ServiceRepository servicesRepository,
            UserRepository userRepository, BusinessServiceRepository businessServiceRepository,
            BusinessReviewRepository businessReviewRepository, BusinessLocationRepository businessLocationRepository,
            FileStorageService fileStorageService, SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
        this.businessRepository = businessRepository;
        this.servicesRepository = servicesRepository;
        this.userRepository = userRepository;
        this.businessServiceRepository = businessServiceRepository;
        this.businessReviewRepository = businessReviewRepository;
        this.businessLocationRepository = businessLocationRepository;
        this.fileStorageService = fileStorageService;
    }

    // // DTO (Simplified for this example)
    // public static class BusinessDTO {
    // public Long id;
    // public String name;
    // public String description;
    // public Long ownerId;
    // public List<Long> categoryIds;
    // public BusinessLocationDTO location;
    // public String phoneNumber;
    // public String email;
    // public String website;
    // public OpeningHours openingHours;
    // public String locationJson;
    // public String categoryIdsJson;
    // public String openingHoursJson;
    // public String socialMediaJson;
    // public SocialMedia socialMedia;
    // public List<String> images;
    // public boolean isVerified;
    // public boolean isFeatured;
    // public String phone;

    // public BusinessDTO() {
    // }

    // public BusinessDTO(Business business) {
    // this.id = business.getId();
    // this.name = business.getName();
    // this.description = business.getDescription();
    // this.ownerId = business.getOwner().getId();
    // this.location = business.getLocation() != null ? new
    // BusinessLocationDTO(business.getLocation()) : null;
    // this.phoneNumber = business.getPhoneNumber();
    // this.email = business.getEmail();
    // this.website = business.getWebsite();
    // this.openingHours = business.getOpeningHours();
    // this.socialMedia = business.getSocialMedia();
    // this.images = business.getImages();
    // this.isVerified = business.isVerified();
    // this.isFeatured = business.isFeatured();
    // this.phone = business.getPhoneNumber();
    // }

    // private static final ObjectMapper mapper = new ObjectMapper()
    // .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // public List<Long> getCategoryIds() {
    // System.out.println("Category IDs: " + categoryIdsJson);
    // System.out.println(categoryIdsJson);
    // if (categoryIdsJson == null || categoryIdsJson.isEmpty()) {
    // return null;
    // }
    // try {
    // return mapper.readValue(categoryIdsJson, new TypeReference<List<Long>>() {
    // });
    // } catch (JsonProcessingException e) {
    // throw new IllegalArgumentException("Invalid location format: " +
    // e.getMessage(), e);
    // }
    // }

    // public BusinessLocationDTO getLocationFromJSON() {
    // System.out.println(locationJson);
    // if (locationJson == null || locationJson.isEmpty()) {
    // return null;
    // }
    // try {
    // return mapper.readValue(locationJson, BusinessLocationDTO.class);
    // } catch (JsonProcessingException e) {
    // throw new IllegalArgumentException("Invalid location format: " +
    // e.getMessage(), e);
    // }
    // }

    // public OpeningHours getOpeningHoursFromJSON() {
    // if (openingHoursJson == null || openingHoursJson.isEmpty()) {
    // return null;
    // }
    // try {
    // return mapper.readValue(openingHoursJson, OpeningHours.class);
    // } catch (JsonProcessingException e) {
    // throw new IllegalArgumentException("Invalid opening hours format: " +
    // e.getMessage(), e);
    // }
    // }

    // public SocialMedia getSocialMediaFromJSON() {
    // if (socialMediaJson == null || socialMediaJson.isEmpty()) {
    // return null;
    // }
    // try {
    // return mapper.readValue(socialMediaJson, SocialMedia.class);
    // } catch (JsonProcessingException e) {
    // throw new IllegalArgumentException("Invalid social media format: " +
    // e.getMessage(), e);
    // }
    // }

    // }

    public BusinessDTO createBusiness(@Valid BusinessRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("Owner not found with ID: " + request.getOwnerId()));

        assignBusinessRoleIfNeeded(owner);

        // Save the BusinessLocation entity first
        System.out.println(request.getLocation());
        BusinessLocation location = new BusinessLocation(request.getLocation());
        if (location != null) {
            location = businessLocationRepository.save(location);
        }

        // Handle image uploads
        List<String> imageUrls = new ArrayList<>();
        if (request.getImages() != null) {
            for (MultipartFile image : request.getImages()) {
                String fileName = fileStorageService.storeFile(image);
                imageUrls.add(fileName);
            }
        }
        System.out.println(request.getName());
        Business business = new Business();
        business.setName(request.getName());
        business.setDescription(request.getDescription());
        business.setOwner(owner);
        business.setLocation(location);
        business.setPhoneNumber(request.getPhoneNumber());
        business.setEmail(request.getEmail());
        business.setWebsite(request.getWebsite());
        business.setOpeningHours(request.getOpeningHours());
        business.setSocialMedia(request.getSocialMedia());
        business.setImages(imageUrls);
        business.setVerified(request.isVerified());
        business.setFeatured(request.isFeatured());

        // Map services
        if (request.getServiceIds() != null) {
            Set<Services> services = request.getServiceIds().stream()
                    .map(id -> servicesRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + id)))
                    .collect(Collectors.toSet());
            business.setServices(services);
        }

        Business savedBusiness = businessRepository.save(business);
        return new BusinessDTO(savedBusiness);
    }

    public Business getBusiness(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Business not found: " + id));
    }

    public Business getBusinessById(Long id, Long currentUserId) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        // if (!business.getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("Unauthorized");
        // }
        return business;
    }

    public BusinessDTO updateBusiness(Long id, BusinessRequest dto, Long currentUserId) {
        Business business = getBusinessById(id, currentUserId);
        business.setName(dto.name);
        business.setEmail(dto.email);
        business.setPhoneNumber(dto.phoneNumber);
        business.setDescription(dto.description);
        business.setWebsite(dto.website);
        business.setOpeningHours(dto.getOpeningHours());
        business.setSocialMedia(dto.getSocialMedia());
        business.setVerified(dto.isVerified);
        business.setFeatured(dto.isFeatured);
        List<String> imageUrls = new ArrayList<>();
        if (dto.getImages() != null) {
            for (MultipartFile image : dto.getImages()) {
                String fileName = fileStorageService.storeFile(image);
                imageUrls.add(fileName);
            }
        }
        BusinessLocation location = businessLocationRepository.save(new BusinessLocation(dto.getLocation()));
        business.setLocation(location);
        
        // Update services
        if (dto.getServiceIds() != null) {
            Set<Services> services = dto.getServiceIds().stream()
                    .map(sid -> servicesRepository.findById(sid)
                            .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + sid)))
                    .collect(Collectors.toSet());
            business.setServices(services);
        }
        Business updatedBusiness = businessRepository.save(business);
        return new BusinessDTO(updatedBusiness);
    }

    public void deleteBusiness(Long id, Long currentUserId) {
        Business business = getBusinessById(id, currentUserId);
        businessRepository.delete(business);
    }

    public Page<BusinessDTO> getAllBusinesses(Pageable pageable, String industry, String location) {
        Page<Business> businesses;
        if (industry != null && location != null) {
            businesses = businessRepository.findByIndustryAndLocation_City(industry, location, pageable);
        } else if (industry != null) {
            businesses = businessRepository.findByIndustry(industry, pageable);
        } else if (location != null) {
            businesses = businessRepository.findByLocation_City(location, pageable);
        } else {
            businesses = businessRepository.findAll(pageable);
        }
        return businesses.map(this::mapBusinessEntityToDTO);
    }

    @Transactional
    public BusinessDTO verifyBusiness(Long id) {
        logger.info("Verifying business with ID: {}", id);
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + id));
        business.setVerified(true);
        Business verifiedBusiness = businessRepository.save(business);
        logger.info("Business verified with ID: {}", verifiedBusiness.getId());
        return mapBusinessEntityToDTO(verifiedBusiness);
    }

    public ProfileViewsDTO getProfileViews(Long businessId) {
        logger.info("Fetching profile views for business ID: {}", businessId);
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));

        // Mock implementation for views (replace with actual analytics tracking)
        ProfileViewsDTO profileViews = new ProfileViewsDTO();
        profileViews.setTotalViews(1000L); // Example value
        List<DailyViewDTO> dailyViews = new ArrayList<>();
        DailyViewDTO dailyView = new DailyViewDTO();
        dailyView.setDate(LocalDateTime.now());
        dailyView.setViews(50L);
        dailyViews.add(dailyView);
        profileViews.setDailyViews(dailyViews);
        return profileViews;
    }

    public Page<Business> searchBusinesses(String query, String locationQuery, int page, int size) {
        return searchBusinesses(query, locationQuery, null, page, size);
    }

    public Page<Business> searchBusinesses(String query, String locationQuery, List<Long> serviceIds, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedQuery = (query == null || query.isBlank()) ? null : query.trim();
        String normalizedLocation = (locationQuery == null || locationQuery.isBlank()) ? null : locationQuery.trim();

        if (serviceIds == null || serviceIds.isEmpty()) {
            return businessRepository.searchByNameOrDescription(normalizedQuery, normalizedLocation, pageable);
        }
        return businessRepository.search(normalizedQuery, normalizedLocation, serviceIds, pageable);
    }

    public Page<BusinessDTO> getFeaturedBusinesses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Business> businesses = businessRepository.findByIsFeaturedTrue(pageable);
        return businesses.map(BusinessDTO::new);
    }

    public Page<BusinessServices> getBusinessServices(Long businessId, Long currentUserId, int page, int size) {
        Business business = getBusinessById(businessId, currentUserId);
        Pageable pageable = PageRequest.of(page, size);
        return businessServiceRepository.findByBusiness(business, pageable);
    }

    public Page<BusinessReview> getBusinessReviews(Long businessId, Long currentUserId, int page, int size) {
        Business business = getBusinessById(businessId, currentUserId);
        Pageable pageable = PageRequest.of(page, size);
        return businessReviewRepository.findByBusiness(business, pageable);
    }

    public BusinessDTO addServiceToBusiness(Long businessId, Long serviceId, Long currentUserId) {
        Business business = getBusinessById(businessId, currentUserId);
        BusinessServices service = businessServiceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
        businessServiceRepository.save(service);
        return new BusinessDTO(business);
    }

    public BusinessDTO uploadLogo(Long id, MultipartFile logo, Long currentUserId) throws IOException {
        Business business = getBusinessById(id, currentUserId);
        String fileName = System.currentTimeMillis() + "_" + logo.getOriginalFilename();
        Path path = Paths.get("uploads/" + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, logo.getBytes());
        business.setLogo("/uploads/" + fileName);
        Business savedBusiness = businessRepository.save(business);
        return new BusinessDTO(savedBusiness);
    }

    public Page<BusinessDTO> getBusinessesByOwner(Long ownerId, int page, int size) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found"));
        Pageable pageable = PageRequest.of(page, size);
        Page<Business> businesses = businessRepository.findByOwner(owner, pageable);
        return businesses.map(BusinessDTO::new);

    }

    public Business updateSubscription(Long id, Long planId) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));
        SubscriptionPlan plan = subscriptionService.getPlanById(planId);
        if (plan.getPlanType() != PlanType.BUSINESS) {
            throw new IllegalArgumentException("Invalid plan type for business");
        }
        business.setSubscriptionPlan(plan);
        return businessRepository.save(business);
    }

    public Business createSubscription(Long id, Long planId) {

        Business business = getBusiness(id);
        if (business.getSubscriptionPlan() != null) {
            throw new IllegalStateException("Business is already subscribed to a plan. Use update instead.");
        }
        SubscriptionPlan plan = subscriptionService.getPlanById(planId);
        if (plan.getPlanType() != PlanType.BUSINESS) {
            throw new IllegalArgumentException("Invalid plan type for business");
        }
        business.setSubscriptionPlan(plan);
        return businessRepository.save(business);
    }

    private void assignBusinessRoleIfNeeded(User owner) {
        if (owner == null) {
            return;
        }

        UserRole currentRole = owner.getRole();
        if (currentRole == null || currentRole == UserRole.USER || currentRole == UserRole.CUSTOMER) {
            owner.setRole(UserRole.BUSINESS);
            userRepository.save(owner);
        }
    }

    private BusinessDTO mapBusinessEntityToDTO(Business entity) {
        BusinessDTO dto = new BusinessDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setBusinessType(entity.getBusinessType());
        dto.setDescription(entity.getDescription());
        dto.setLogo(entity.getLogo());
        dto.setWebsite(entity.getWebsite());
        dto.setFoundedYear(entity.getFoundedYear());
        dto.setEmployeeCount(entity.getEmployeeCount());
        dto.setVerified(entity.isVerified());
        dto.setIndustry(entity.getIndustry());
        dto.setTaxId(entity.getTaxId());
        dto.setCertifications(entity.getCertifications());
        dto.setMinOrderQuantity(entity.getMinOrderQuantity());
        dto.setTradeTerms(entity.getTradeTerms());
        dto.setImages(entity.getImages());
        dto.setFeatured(entity.isFeatured());
        dto.setOwner(entity.getOwner() != null ? new BusinessDTO.OwnerDTO(entity.getOwner()) : null);
        dto.setTelephoneNumbers(entity.getTelephoneNumbers());
        dto.setMobileNumbers(entity.getMobileNumbers());
        // Note: Map relationships as needed
        return dto;
    }

}