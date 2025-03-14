package com.home.service.Service;

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
import com.home.service.dto.BusinessRequest;
import com.home.service.models.Business;
import com.home.service.models.BusinessLocation;
import com.home.service.models.BusinessReview;
import com.home.service.models.BusinessServices;
import com.home.service.models.Services;
import com.home.service.models.User;
import com.home.service.models.enums.BusinessType;
import com.home.service.models.enums.OpeningHours;
import com.home.service.models.enums.SocialMedia;
import com.home.service.repositories.BusinessLocationRepository;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.BusinessReviewRepository;
import com.home.service.repositories.BusinessServiceRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final ServiceRepository servicesRepository; // For category validation
    private final UserRepository userRepository;
    private final BusinessServiceRepository businessServiceRepository;
    private final BusinessReviewRepository businessReviewRepository;
    private final BusinessLocationRepository businessLocationRepository;
    private final FileStorageService fileStorageService;

    public BusinessService(BusinessRepository businessRepository, ServiceRepository servicesRepository,
            UserRepository userRepository, BusinessServiceRepository businessServiceRepository,
            BusinessReviewRepository businessReviewRepository, BusinessLocationRepository businessLocationRepository,
            FileStorageService fileStorageService) {
        this.businessRepository = businessRepository;
        this.servicesRepository = servicesRepository;
        this.userRepository = userRepository;
        this.businessServiceRepository = businessServiceRepository;
        this.businessReviewRepository = businessReviewRepository;
        this.businessLocationRepository = businessLocationRepository;
        this.fileStorageService = fileStorageService;
    }

    // DTO (Simplified for this example)
    public static class BusinessDTO {
        public Long id;
        public String name;
        public String description;
        public Long ownerId;
        public List<Long> categoryIds;
        public String categoryIdsJson;
        public BusinessLocationDTO location;
        public String locationJson;
        public String phoneNumber;
        public String email;
        public String website;
        public OpeningHours openingHours;
        public String openingHoursJson;
        public SocialMedia socialMedia;
        public String socialMediaJson;
        public List<String> images;
        public boolean isVerified;
        public boolean isFeatured;
        public String phone;

        public BusinessDTO() {
        }

        public BusinessDTO(Business business) {
            this.id = business.getId();
            this.name = business.getName();
            this.description = business.getDescription();
            this.ownerId = business.getOwner().getId();
            this.location = business.getLocation() != null ? new BusinessLocationDTO(business.getLocation()) : null;
            this.phoneNumber = business.getPhoneNumber();
            this.email = business.getEmail();
            this.website = business.getWebsite();
            this.openingHours = business.getOpeningHours();
            this.socialMedia = business.getSocialMedia();
            this.images = business.getImages();
            this.isVerified = business.isVerified();
            this.isFeatured = business.isFeatured();
            this.phone = business.getPhoneNumber();
        }

        private static final ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        public List<Long> getCategoryIds() {
            System.out.println("Category IDs: " + categoryIdsJson);
            System.out.println(categoryIdsJson);
            if (categoryIdsJson == null || categoryIdsJson.isEmpty()) {
                return null;
            }
            try {
                return mapper.readValue(categoryIdsJson, new TypeReference<List<Long>>() {
                });
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid location format: " + e.getMessage(), e);
            }
        }

        public BusinessLocationDTO getLocationFromJSON() {
            System.out.println(locationJson);
            if (locationJson == null || locationJson.isEmpty()) {
                return null;
            }
            try {
                return mapper.readValue(locationJson, BusinessLocationDTO.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid location format: " + e.getMessage(), e);
            }
        }

        public OpeningHours getOpeningHoursFromJSON() {
            if (openingHoursJson == null || openingHoursJson.isEmpty()) {
                return null;
            }
            try {
                return mapper.readValue(openingHoursJson, OpeningHours.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid opening hours format: " + e.getMessage(), e);
            }
        }

        public SocialMedia getSocialMediaFromJSON() {
            if (socialMediaJson == null || socialMediaJson.isEmpty()) {
                return null;
            }
            try {
                return mapper.readValue(socialMediaJson, SocialMedia.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid social media format: " + e.getMessage(), e);
            }
        }

    }

    public BusinessDTO createBusiness(@Valid BusinessRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("Owner not found with ID: " + request.getOwnerId()));

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

        Business savedBusiness = businessRepository.save(business);
        return new BusinessDTO(savedBusiness);
    }

    public Business getBusinessById(Long id, Long currentUserId) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        // if (!business.getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("Unauthorized");
        // }
        return business;
    }

    public BusinessDTO updateBusiness(Long id, BusinessDTO dto, Long currentUserId) {
        Business business = getBusinessById(id, currentUserId);
        business.setName(dto.name);
        business.setEmail(dto.email);
        business.setPhoneNumber(dto.phoneNumber);
        business.setDescription(dto.description);
        business.setWebsite(dto.website);
        business.setOpeningHours(dto.getOpeningHoursFromJSON());
        business.setSocialMedia(dto.getSocialMediaFromJSON());
        business.setImages(dto.images);
        business.setVerified(dto.isVerified);
        business.setFeatured(dto.isFeatured);
        BusinessLocation location = businessLocationRepository.save(new BusinessLocation(dto.getLocationFromJSON()));
        business.setLocation(location);
        if (dto.getCategoryIds() != null) {
            List<Services> categories = servicesRepository.findAllById(dto.getCategoryIds());
            if (categories.size() != dto.categoryIds.size()) {
                throw new BadRequestException("One or more category IDs are invalid");
            }
            business.setCategories(categories);
        }
        Business updatedBusiness = businessRepository.save(business);
        return new BusinessDTO(updatedBusiness);
    }

    public void deleteBusiness(Long id, Long currentUserId) {
        Business business = getBusinessById(id, currentUserId);
        businessRepository.delete(business);
    }

    public Page<BusinessDTO> getAllBusinesses(Long locationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (locationId != null) {
            Page<Business> businesses = businessRepository.findByLocationId(locationId, pageable);
            return businesses.map(BusinessDTO::new);
        }
        Page<Business> businesses = businessRepository.findAll(pageable);
        return businesses.map(BusinessDTO::new);
    }

    public Page<Business> searchBusinesses(String query, Long locationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Assume a custom repository method for full-text search
        return businessRepository.searchByNameOrDescription(query, locationId, pageable);
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

}