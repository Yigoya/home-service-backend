package com.home.service.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.BusinessClaimService;
import com.home.service.Service.BusinessLocationService;
import com.home.service.Service.BusinessLocationService.BusinessLocationDTO;
import com.home.service.Service.BusinessPromotionService.BusinessPromotionDTO;
import com.home.service.Service.BusinessPromotionService;
import com.home.service.Service.BusinessReviewService;
import com.home.service.Service.BusinessReviewService.BusinessReviewDTO;
import com.home.service.Service.BusinessService;
import com.home.service.Service.BusinessServiceService;
import com.home.service.Service.BusinessServiceService.BusinessServiceDTO;
import com.home.service.Service.EnquiryService.EnquiryDTO;
import com.home.service.Service.ProductService;
import com.home.service.Service.EnquiryService;
import com.home.service.Service.SavedContentService;
import com.home.service.Service.SearchLogService;
import com.home.service.Service.BusinessClaimService.BusinessClaimDTO;
import com.home.service.dto.SearchLogAnalyticsDTO;
import com.home.service.models.Business;
import com.home.service.models.BusinessClaim;
import com.home.service.models.BusinessLocation;
import com.home.service.models.BusinessPromotion;
import com.home.service.models.BusinessReview;
import com.home.service.models.BusinessServices;
import com.home.service.models.Enquiry;
import com.home.service.models.SearchLog;
import com.home.service.models.enums.ClaimStatus;
import com.home.service.models.enums.EnquiryStatus;
import com.home.service.models.enums.PromotionType;
import java.util.Set;
import com.home.service.dto.BusinessDTO;
import com.home.service.dto.BusinessRequest;
import com.home.service.dto.ReviewRequest;
import com.home.service.dto.BusinessServiceRequest;
import com.home.service.dto.PromotionRequest;
import com.home.service.models.enums.ReviewStatus;
import com.home.service.services.FileStorageService;

@RestController
@RequestMapping("/businesses")
public class BusinessController {

    private final BusinessService businessService;
    private final BusinessReviewService businessReviewService;
    private final BusinessLocationService businessLocationService;
    private final EnquiryService enquiryService;
    private final BusinessClaimService businessClaimService;
    private final BusinessServiceService businessServiceService;
    private final BusinessPromotionService businessPromotionService;
    private final SearchLogService searchLogService;
    // private final ProductService productService; // Removed unused field
    private final SavedContentService savedContentService;
    private final FileStorageService fileStorageService;

    public BusinessController(BusinessService businessService, BusinessReviewService businessReviewService,
            BusinessLocationService businessLocationService, EnquiryService enquiryService,
            BusinessClaimService businessClaimService, BusinessServiceService businessServiceService,
            BusinessPromotionService businessPromotionService, SearchLogService searchLogService,
            ProductService productService, SavedContentService savedContentService,
            FileStorageService fileStorageService) {
        this.searchLogService = searchLogService;
        this.businessPromotionService = businessPromotionService;
        this.businessServiceService = businessServiceService;
        this.businessClaimService = businessClaimService;
        this.enquiryService = enquiryService;
        this.businessLocationService = businessLocationService;
        this.businessReviewService = businessReviewService;
        this.businessService = businessService;
    // this.productService = productService;
        this.savedContentService = savedContentService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BusinessDTO> createBusiness(@ModelAttribute BusinessRequest request) {
        BusinessDTO business = businessService.createBusiness(request);
        return new ResponseEntity<>(business, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessDTO> getBusinessById(@PathVariable Long id) {

        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        Business business = businessService.getBusinessById(id, currentUserId);
        return ResponseEntity.ok(new BusinessDTO(business));
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/{id}")
    public ResponseEntity<BusinessDTO> updateBusiness(@PathVariable Long id,
            @RequestBody BusinessRequest businessRequest) {
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        BusinessDTO business = businessService.updateBusiness(id, businessRequest, currentUserId);
        return ResponseEntity.ok(business);
    }

    @CrossOrigin(originPatterns = "*")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable Long id) {
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        businessService.deleteBusiness(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getBusinessDetails(@PathVariable Long id) {
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        Business business = businessService.getBusinessById(id, currentUserId);
        System.out.println(business.getLocation());
        BusinessDTO businessDTO = new BusinessDTO(business);

        // Get reviews
        Page<BusinessReviewDTO> reviews = businessReviewService.getReviewDTOsByBusiness(id, 0, 100);

        // Get services
        Page<BusinessServiceDTO> services = businessServiceService.getServicesByBusiness(id, 0, 100);

        Map<String, Object> response = new HashMap<>();
        response.put("business", businessDTO);
        response.put("reviews", reviews.getContent());
        response.put("services", services.getContent());
        response.put("gallery", businessDTO.getImages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BusinessDTO>> searchBusinesses(
            @RequestParam String query,
            @RequestParam(required = false) String locationQuery,
            @RequestParam(required = false, name = "serviceIds") List<Long> serviceIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Business> businesses = businessService.searchBusinesses(query, locationQuery, serviceIds, page, size);
        Page<BusinessDTO> businessDTOs = businesses.map(BusinessDTO::new);
        return ResponseEntity.ok(businessDTOs);
    }

    @GetMapping("/featured")
    public ResponseEntity<Page<BusinessDTO>> getFeaturedBusinesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessDTO> businesses = businessService.getFeaturedBusinesses(page, size);
        return ResponseEntity.ok(businesses);
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<Page<BusinessServices>> getBusinessServices(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessServices> services = businessService.getBusinessServices(id, null, page, size);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Page<BusinessReview>> getBusinessReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessReview> reviews = businessReviewService.getReviewsByBusiness(id,
        page, size);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<BusinessDTO>> getBusinessesByOwner(
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessDTO> businesses = businessService.getBusinessesByOwner(ownerId, page, size);
        return ResponseEntity.ok(businesses);
    }

    @PostMapping("/{companyId}/logo")
    public ResponseEntity<BusinessDTO> uploadLogo(@PathVariable Long companyId,
            @RequestParam("logo") MultipartFile logo)
            throws IOException {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        return new ResponseEntity<>(businessService.uploadLogo(companyId, logo, currentUserId), HttpStatus.OK);
    }

    // Service Endpoints
    @PostMapping(value = "/services", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BusinessServiceDTO> createBusinessService(@ModelAttribute BusinessServiceRequest request) {
        BusinessServiceDTO service = businessServiceService.createBusinessService(request);
        return new ResponseEntity<>(service, HttpStatus.CREATED);
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<BusinessServiceDTO> getBusinessServiceById(@PathVariable Long id) {
        BusinessServices service = businessServiceService.getBusinessServiceById(id);

        return ResponseEntity.ok(new BusinessServiceDTO(service));
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping(value = "/services/{id}")
    public ResponseEntity<BusinessServiceDTO> updateBusinessService(
            @PathVariable Long id,
            @ModelAttribute BusinessServiceRequest request) {
        BusinessServiceDTO service = businessServiceService.updateBusinessService(id, request);
        return ResponseEntity.ok(service);
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteBusinessService(@PathVariable Long id) {
        businessServiceService.deleteBusinessService(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services/business/{businessId}")
    public ResponseEntity<Page<BusinessServiceDTO>> getServicesByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessServiceDTO> services = businessServiceService.getServicesByBusiness(businessId, page, size);
        return ResponseEntity.ok(services);
    }

    @PostMapping(value = "/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BusinessReviewDTO> createReview(@ModelAttribute ReviewRequest request) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        BusinessReviewDTO review = businessReviewService.createReview(request, currentUserId);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<BusinessReviewDTO> getReviewById(@PathVariable Long id) {
        BusinessReview review = businessReviewService.getReviewById(id);
        return ResponseEntity.ok(new BusinessReviewDTO(review));
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/reviews/{id}")
    public ResponseEntity<BusinessReviewDTO> updateReview(@PathVariable Long id,
            @RequestBody BusinessReviewService.BusinessReviewDTO reviewDTO) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        BusinessReviewDTO review = businessReviewService.updateReview(id, reviewDTO, currentUserId);
        return ResponseEntity.ok(review);
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        businessReviewService.deleteReview(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews/business/{businessId}")
    public ResponseEntity<Page<BusinessReviewDTO>> getReviewsByBusiness(
            @PathVariable Long businessId,
            @RequestParam(required = false, defaultValue = "all") String dateRange,
            @RequestParam(required = false, defaultValue = "all") String rating,
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
    Page<BusinessReviewDTO> reviews = businessReviewService.getReviewsByBusiness(businessId, dateRange, rating,
                status, page, size);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/reviews/{reviewId}/respond")
    public ResponseEntity<BusinessReviewDTO> respondToReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> request) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        String response = request.get("response");
        if (response == null || response.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        BusinessReviewDTO review = businessReviewService.respondToReview(reviewId, response, currentUserId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/reviews/user/{userId}")
    public ResponseEntity<Page<BusinessReviewDTO>> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessReviewDTO> reviews = businessReviewService.getReviewsByUser(userId, page, size);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/locations")
    public ResponseEntity<BusinessLocationDTO> createLocation(
            @RequestBody BusinessLocationService.BusinessLocationDTO locationDTO) {
        BusinessLocationDTO location = businessLocationService.createLocation(locationDTO);
        return new ResponseEntity<>(location, HttpStatus.CREATED);
    }

    @GetMapping("/locations/{id}")
    public ResponseEntity<BusinessLocation> getLocationById(@PathVariable Long id) {
        BusinessLocation location = businessLocationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/locations/{id}")
    public ResponseEntity<BusinessLocationDTO> updateLocation(@PathVariable Long id,
            @RequestBody BusinessLocationService.BusinessLocationDTO locationDTO) {
        BusinessLocationDTO location = businessLocationService.updateLocation(id, locationDTO);
        return ResponseEntity.ok(location);
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/locations/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        businessLocationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/locations")
    public ResponseEntity<Page<BusinessLocationDTO>> getAllLocations(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessLocationDTO> locations = businessLocationService.getAllLocations(type, page, size);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/locations/nearby")
    public ResponseEntity<Page<BusinessLocationDTO>> getNearbyLocations(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radius,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessLocationDTO> locations = businessLocationService.getNearbyLocations(latitude, longitude, radius,
                page,
                size);
        return ResponseEntity.ok(locations);
    }

    @PostMapping("/enquiries")
    public ResponseEntity<EnquiryDTO> createEnquiry(@RequestBody EnquiryDTO enquiryDTO) {
        // Long currentUserId = SecurityContextHolder.getContext().getAuthentication()
        // != null
        // ? SecurityContextHolder.getContext().getAuthentication().getName()
        // : null;
        Long currentUserId = 4L;
        EnquiryDTO enquiry = enquiryService.createEnquiry(enquiryDTO, currentUserId);
        return new ResponseEntity<>(enquiry, HttpStatus.CREATED);
    }

    @GetMapping("/enquiries/{id}")
    public ResponseEntity<EnquiryDTO> getEnquiryById(@PathVariable Long id) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        Enquiry enquiry = enquiryService.getEnquiryById(id, currentUserId);
        return ResponseEntity.ok(new EnquiryDTO(enquiry));
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/enquiries/{id}/status")
    public ResponseEntity<EnquiryDTO> updateEnquiryStatus(
            @PathVariable Long id,
            @RequestParam EnquiryStatus status) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        EnquiryDTO enquiry = enquiryService.updateEnquiryStatus(id, status, currentUserId);
        return ResponseEntity.ok(enquiry);
    }

    @GetMapping("/enquiries/business/{businessId}")
    public ResponseEntity<Page<EnquiryDTO>> getEnquiriesByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        Page<EnquiryDTO> enquiries = enquiryService.getEnquiriesByBusiness(businessId, page, size, currentUserId);
        return ResponseEntity.ok(enquiries);
    }

    @GetMapping("/enquiries/user/{userId}")
    public ResponseEntity<Page<EnquiryDTO>> getEnquiriesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EnquiryDTO> enquiries = enquiryService.getEnquiriesByUser(userId, page, size);
        return ResponseEntity.ok(enquiries);
    }

    @PostMapping(value = "/promotions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BusinessPromotionDTO> createPromotion(@ModelAttribute PromotionRequest request) {
        Long currentUserId = 4L; // TODO replace with authenticated user id

        // Map request to service DTO
        BusinessPromotionDTO dto = new BusinessPromotionDTO();
        dto.businessId = request.getBusinessId();
        dto.title = request.getTitle();
        dto.description = request.getDescription();
        dto.startDate = request.getStartDate();
        dto.endDate = request.getEndDate();
        dto.type = request.getType();
        dto.discountPercentage = request.getDiscountPercentage();
        dto.isFeatured = request.getIsFeatured() != null ? request.getIsFeatured() : false;
        dto.serviceIds = request.getServiceIds();
        dto.termsAndConditions = request.getTermsAndConditions();

        // Handle image upload via storage service if present
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String storedFileName = fileStorageService.storeFile(request.getImage());
                dto.imageUrl = storedFileName; // Assuming static handler maps /uploads/** to /opt/uploads/
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

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/promotions/{id}")
    public ResponseEntity<BusinessPromotionDTO> updatePromotion(@PathVariable Long id,
            @RequestBody BusinessPromotionService.BusinessPromotionDTO promotionDTO) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        BusinessPromotionDTO promotion = businessPromotionService.updatePromotion(id, promotionDTO, currentUserId);
        return ResponseEntity.ok(promotion);
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/promotions/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        businessPromotionService.deletePromotion(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/promotions/business/{businessId}")
    public ResponseEntity<Page<BusinessPromotionDTO>> getPromotionsByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessPromotionDTO> promotions = businessPromotionService.getPromotionsByBusiness(businessId, page,
                size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/active")
    public ResponseEntity<Page<BusinessPromotionDTO>> getActivePromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessPromotionDTO> promotions = businessPromotionService.getActivePromotions(page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/search-logs/{id}")
    public ResponseEntity<SearchLog> getSearchLogById(@PathVariable Long id) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        SearchLog log = searchLogService.getSearchLogById(id, currentUserId);
        return ResponseEntity.ok(log);
    }

    @GetMapping("/search-logs")
    public ResponseEntity<Page<SearchLog>> getAllSearchLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        Page<SearchLog> logs = searchLogService.getAllSearchLogs(page, size, currentUserId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/search-logs/user/{userId}")
    public ResponseEntity<Page<SearchLog>> getSearchLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        Page<SearchLog> logs = searchLogService.getSearchLogsByUser(userId, page, size, currentUserId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/search-logs/analytics")
    public ResponseEntity<Page<SearchLogAnalyticsDTO>> getSearchAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        Page<SearchLogAnalyticsDTO> analytics = searchLogService.getSearchAnalytics(startDate, endDate,
                page, size, currentUserId);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/claims")
    public ResponseEntity<BusinessClaimDTO> createClaim(@RequestBody BusinessClaimDTO claimDTO) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        BusinessClaimDTO claim = businessClaimService.createClaim(claimDTO, currentUserId);
        return new ResponseEntity<>(claim, HttpStatus.CREATED);
    }

    @GetMapping("/claims/{id}")
    public ResponseEntity<BusinessClaimDTO> getClaimById(@PathVariable Long id) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        BusinessClaim claim = businessClaimService.getClaimById(id, currentUserId);

        return ResponseEntity.ok(new BusinessClaimDTO(claim));
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/claims/{id}/status")
    public ResponseEntity<BusinessClaimDTO> updateClaimStatus(
            @PathVariable Long id,
            @RequestParam ClaimStatus status) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        BusinessClaimDTO claim = businessClaimService.updateClaimStatus(id, status, currentUserId);
        return ResponseEntity.ok(claim);
    }

    @GetMapping("/claims/business/{businessId}")
    public ResponseEntity<List<BusinessClaimDTO>> getClaimsByBusiness(@PathVariable Long businessId) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;
        List<BusinessClaimDTO> claims = businessClaimService.getClaimsByBusiness(businessId, currentUserId);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/claims/user/{userId}")
    public ResponseEntity<Page<BusinessClaimDTO>> getClaimsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessClaimDTO> claims = businessClaimService.getClaimsByUser(userId, page, size);
        return ResponseEntity.ok(claims);
    }

    // @GetMapping("/products")
    // public ResponseEntity<Page<ProductDTO>> getProducts(
    // @PathVariable Long companyId,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "20") int size,
    // @RequestParam(defaultValue = "createdAt,desc") String sort,
    // @RequestParam(required = false) String search) {
    // // String currentUserId =
    // // SecurityContextHolder.getContext().getAuthentication().getName();
    // Long currentUserId = 4L;
    // return ResponseEntity.ok(productService.getProducts(companyId, page, size,
    // sort, search, currentUserId));
    // }

    // @PostMapping("/products")
    // public ResponseEntity<ProductDTO> createProduct(@PathVariable Long companyId,
    // @RequestBody ProductDTO dto) {
    // // String currentUserId =
    // // SecurityContextHolder.getContext().getAuthentication().getName();
    // Long currentUserId = 4L;
    // return new ResponseEntity<>(productService.createProduct(companyId, dto,
    // currentUserId), HttpStatus.CREATED);
    // }

    // @CrossOrigin(originPatterns = "*")
    // @PutMapping("/products/{productId}")
    // public ResponseEntity<ProductDTO> updateProduct(
    // @PathVariable Long companyId,
    // @PathVariable Long productId,
    // @RequestBody ProductService.ProductDTO dto) {
    // String currentUserId =
    // SecurityContextHolder.getContext().getAuthentication().getName();
    // return ResponseEntity.ok(productService.updateProduct(companyId, productId,
    // dto, currentUserId));
    // }

    // @CrossOrigin(originPatterns = "*")
@DeleteMapping("/products/{productId}")
    // public ResponseEntity<Void> deleteProduct(@PathVariable Long companyId,
    // @PathVariable Long productId) {
    // String currentUserId =
    // SecurityContextHolder.getContext().getAuthentication().getName();
    // productService.deleteProduct(companyId, productId, currentUserId);
    // return ResponseEntity.noContent().build();
    // }

    @PatchMapping("/reviews/{reviewId}/status")
    public ResponseEntity<BusinessReviewDTO> updateReviewStatus(
            @PathVariable Long reviewId,
            @RequestParam ReviewStatus status) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L;

        // Validate that status is either FLAGGED or ARCHIVED
        if (status != ReviewStatus.FLAGGED && status != ReviewStatus.ARCHIVED) {
            return ResponseEntity.badRequest().build();
        }

        BusinessReviewDTO review = businessReviewService.updateReviewStatus(reviewId, status, currentUserId);
        return ResponseEntity.ok(review);
    }

    // Saved Business Endpoints
    @CrossOrigin(originPatterns = "*")
    @PostMapping("/{businessId}/save")
    public ResponseEntity<String> saveBusiness(@PathVariable Long businessId, @RequestParam Long customerId) {
        savedContentService.saveBusiness(businessId, customerId);
        return ResponseEntity.ok("Business saved successfully");
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/{businessId}/unsave")
    public ResponseEntity<Void> unsaveBusiness(@PathVariable Long businessId, @RequestParam Long customerId) {
        savedContentService.unsaveBusiness(businessId, customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{businessId}/is-saved")
    public ResponseEntity<Boolean> isBusinessSaved(@PathVariable Long businessId, @RequestParam Long customerId) {
        boolean isSaved = savedContentService.isBusinessSaved(businessId, customerId);
        return ResponseEntity.ok(isSaved);
    }

    // Public Promotion Endpoints for Customers and Users
    @GetMapping("/promotions/public")
    public ResponseEntity<Page<BusinessPromotionService.PublicPromotionDTO>> getPublicPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<BusinessPromotionService.PublicPromotionDTO> promotions = businessPromotionService.getPublicActivePromotions(page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/public/{id}")
    public ResponseEntity<BusinessPromotionService.PublicPromotionDTO> getPublicPromotionById(@PathVariable Long id) {
        BusinessPromotionService.PublicPromotionDTO promotion = businessPromotionService.getPublicPromotionById(id);
        return ResponseEntity.ok(promotion);
    }

    @GetMapping("/promotions/public/featured")
    public ResponseEntity<Page<BusinessPromotionService.PublicPromotionDTO>> getFeaturedPublicPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Page<BusinessPromotionService.PublicPromotionDTO> promotions = businessPromotionService.getFeaturedPromotions(page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/public/type/{type}")
    public ResponseEntity<Page<BusinessPromotionService.PublicPromotionDTO>> getPromotionsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            PromotionType promotionType = PromotionType.valueOf(type.toUpperCase());
            Page<BusinessPromotionService.PublicPromotionDTO> promotions = businessPromotionService.getPromotionsByType(promotionType, page, size);
            return ResponseEntity.ok(promotions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/promotions/public/industry/{industry}")
    public ResponseEntity<Page<BusinessPromotionService.PublicPromotionDTO>> getPromotionsByIndustry(
            @PathVariable String industry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<BusinessPromotionService.PublicPromotionDTO> promotions = businessPromotionService.getPromotionsByIndustry(industry, page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/public/business-type/{businessType}")
    public ResponseEntity<Page<BusinessPromotionService.PublicPromotionDTO>> getPromotionsByBusinessType(
            @PathVariable String businessType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            com.home.service.models.enums.BusinessType type = com.home.service.models.enums.BusinessType.valueOf(businessType.toUpperCase());
            Page<BusinessPromotionService.PublicPromotionDTO> promotions = businessPromotionService.getPromotionsByBusinessType(type, page, size);
            return ResponseEntity.ok(promotions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/promotions/public/search")
    public ResponseEntity<Page<BusinessPromotionService.PublicPromotionDTO>> searchPublicPromotions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Page<BusinessPromotionService.PublicPromotionDTO> promotions = businessPromotionService.searchPromotions(query.trim(), page, size);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/public/business/{businessId}")
    public ResponseEntity<Page<BusinessPromotionService.PublicPromotionDTO>> getPublicPromotionsByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessPromotionService.PublicPromotionDTO> promotions = businessPromotionService.getActivePromotionsByBusiness(businessId, page, size);
        return ResponseEntity.ok(promotions);
    }

    // Enhanced Promotion Management Endpoints
    @CrossOrigin(originPatterns = "*")
    @PatchMapping("/promotions/{id}/featured")
    public ResponseEntity<BusinessPromotionService.BusinessPromotionDTO> makePromotionFeatured(
            @PathVariable Long id) {
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        BusinessPromotionService.BusinessPromotionDTO promotion = businessPromotionService.makePromotionFeatured(id, currentUserId);
        return ResponseEntity.ok(promotion);
    }

    @CrossOrigin(originPatterns = "*")
    @PatchMapping("/promotions/{id}/unfeatured")
    public ResponseEntity<BusinessPromotionService.BusinessPromotionDTO> removePromotionFeatured(
            @PathVariable Long id) {
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        BusinessPromotionService.BusinessPromotionDTO promotion = businessPromotionService.removePromotionFeatured(id, currentUserId);
        return ResponseEntity.ok(promotion);
    }

    @GetMapping("/promotions/{id}/details")
    public ResponseEntity<BusinessPromotionService.BusinessPromotionDTO> getPromotionDetails(
            @PathVariable Long id) {
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        BusinessPromotionService.BusinessPromotionDTO promotion = businessPromotionService.getPromotionDetails(id, currentUserId);
        return ResponseEntity.ok(promotion);
    }

    @GetMapping("/{businessId}/services/for-promotion")
    public ResponseEntity<Set<BusinessPromotionService.ServiceInfo>> getBusinessServicesForPromotion(
            @PathVariable Long businessId) {
        Set<BusinessPromotionService.ServiceInfo> services = businessPromotionService.getBusinessServices(businessId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/promotions/public/service/{serviceId}")
    public ResponseEntity<Page<BusinessPromotionService.PublicPromotionDTO>> getPromotionsByServiceId(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusinessPromotionService.PublicPromotionDTO> promotions = businessPromotionService.getPromotionsByServiceId(serviceId, page, size);
        return ResponseEntity.ok(promotions);
    }

    // @GetMapping("/promotions/public/service/{serviceId}/featured")
    // public ResponseEntity<BusinessPromotionService.PromotionWithRelatedDTO> getFeaturedPromotionWithRelated(
    //         @PathVariable Long serviceId,
    //         @RequestParam(defaultValue = "5") int relatedCount) {
    //     BusinessPromotionService.PromotionWithRelatedDTO result = businessPromotionService.getFeaturedPromotionWithRelated(serviceId, relatedCount);
    //     return ResponseEntity.ok(result);
    // }
}