package com.home.service.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.B2BOrderService;
import com.home.service.Service.BusinessService;
import com.home.service.Service.InquiryService;
import com.home.service.Service.ProductService;
import com.home.service.dto.B2BOrderDTO;
import com.home.service.dto.BulkProductUploadDTO;
import com.home.service.dto.BusinessDTO;
import com.home.service.dto.BusinessRequest;
import com.home.service.dto.InquiryAnalyticsDTO;
import com.home.service.dto.InquiryDTO;
import com.home.service.dto.InquiryResponseDTO;
import com.home.service.dto.ProductDTO;
import com.home.service.dto.ProductPerformanceDTO;
import com.home.service.dto.ProductRequest;
import com.home.service.dto.ProfileViewsDTO;
import com.home.service.dto.ServiceProductsDTO;
import com.home.service.models.Business;
import com.home.service.models.enums.B2BOrderStatus;
import com.home.service.services.FileStorageService;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/marketplace")
public class MarketplaceController {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private B2BOrderService b2bOrderService;

    // Removed unused ServiceService injection

    @Autowired
    private FileStorageService fileStorageService;

    // Business Endpoints
    @PostMapping("/businesses")
    public ResponseEntity<BusinessDTO> createBusiness(@Valid @RequestBody BusinessRequest businessRequest) {
        return ResponseEntity.ok(businessService.createBusiness(businessRequest));
    }
    
    @CrossOrigin(originPatterns = "*")
    @PutMapping("/businesses/{id}")
    public ResponseEntity<BusinessDTO> updateBusiness(@PathVariable Long id,
            @Valid @RequestBody BusinessRequest businessRequest) {
        Long currentId = 1L; // Replace with actual business ID from authentication context
        return ResponseEntity.ok(businessService.updateBusiness(id, businessRequest, currentId));
    }

    @GetMapping("/businesses/{id}")
    public ResponseEntity<BusinessDTO> getBusiness(@PathVariable Long id) {
        Business business = businessService.getBusiness(id);
        BusinessDTO businessDTO = new BusinessDTO(business);
        return ResponseEntity.ok(businessDTO);
    }

    @GetMapping("/businesses")
    public ResponseEntity<Page<BusinessDTO>> getAllBusinesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(businessService.getAllBusinesses(PageRequest.of(page, size), industry, location));
    }

    // New: Get businesses by owner id
    /**
     * Get paginated businesses owned by a specific user
     * Example: GET /marketplace/businesses/by-owner/123?page=0&size=10
     */
    @GetMapping("/businesses/by-owner/{ownerId}")
    public ResponseEntity<Page<BusinessDTO>> getBusinessesByOwner(
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(businessService.getBusinessesByOwner(ownerId, page, size));
    }

    @PostMapping("/businesses/{id}/verify")
    public ResponseEntity<BusinessDTO> verifyBusiness(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.verifyBusiness(id));
    }

    // Product Endpoints
    @PostMapping(value = "/products", consumes = { "multipart/form-data" })
    public ResponseEntity<ProductDTO> createProduct(@Valid @ModelAttribute ProductRequest productRequest) {
        System.out.println("Received product request: " + productRequest);
        System.out.println("Service IDs: " + productRequest.getServiceIds());
        ProductDTO productDTO = productRequest.toProductDTO();
        System.out.println("Converted product request to DTO: " + productDTO);

        // Process and store images if provided
        if (productRequest.getImages() != null && productRequest.getImages().length > 0) {
            List<String> imagePaths = new ArrayList<>();
            for (var imageFile : productRequest.getImages()) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    String imagePath = fileStorageService.storeFile(imageFile);
                    imagePaths.add(imagePath);
                }
            }
            productDTO.setImages(imagePaths);
        }

        return ResponseEntity.ok(productService.createProduct(productDTO));
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping(value = "/products/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductRequest productRequest) {

        ProductDTO productDTO = productRequest.toProductDTO();
        productDTO.setId(id); // Ensure ID is set correctly

        // Process and store images if provided
        if (productRequest.getImages() != null && productRequest.getImages().length > 0) {
            List<String> imagePaths = new ArrayList<>();
            for (var imageFile : productRequest.getImages()) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    String imagePath = fileStorageService.storeFile(imageFile);
                    imagePaths.add(imagePath);
                }
            }
            productDTO.setImages(imagePaths);
        }

        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return ResponseEntity
                .ok(productService.searchProducts(PageRequest.of(page, size), keyword, category, minPrice, maxPrice));
    }

    // New: Get Products by Business with filtering queries for app use
    /**
     * Get paginated products for a business with optional filters.
     *
     * Query params:
     * - search: keyword to match in name/description
     * - category: exact category string
     * - minPrice/maxPrice: price bounds
     * - inStock: filter by stock availability
     * - active: filter by active flag
     * - serviceId: filter by linked service id
     * - page/size: pagination
     * - sortBy: property to sort by (e.g., createdAt, price, name)
     * - sortDir: asc|desc
     */
    @GetMapping("/businesses/{businessId}/products")
    public ResponseEntity<Page<ProductDTO>> getProductsByBusiness(
        @PathVariable Long businessId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) Boolean inStock,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) Long serviceId,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
        @RequestParam(required = false, defaultValue = "desc") String sortDir) {
    Page<ProductDTO> result = productService.getProductsByBusinessWithFilters(
        businessId, search, category, minPrice, maxPrice, inStock, active, serviceId, page, size, sortBy,
        sortDir);
    return ResponseEntity.ok(result);
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Created: Get Products by Service
    @GetMapping("/products/by-service/{serviceId}")
    public ResponseEntity<Page<ProductDTO>> getProductsByService(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getProductsByService(serviceId, PageRequest.of(page, size)));
    }

    // Created: Search Products by Service and Name
    /**
     * Search products linked to a service with optional advanced filters.
     * Existing behavior (serviceId + name) is preserved; new filters are optional.
     *
     * Query params:
     * - serviceId (required): ID of service to filter products by
     * - name (optional): keyword contained in product name
     * - category (optional): exact category match
     * - minPrice / maxPrice (optional): numeric bounds
     * - inStock (optional): filter by stock availability
     * - active (optional): include inactive if false; defaults to true
     * - sortBy (optional): field to sort by (default createdAt)
     * - sortDir (optional): asc | desc (default desc)
     * - page / size: pagination
     */
    @GetMapping("/products/search-by-service")
    public ResponseEntity<Page<ProductDTO>> searchProductsByServiceAndName(
        @RequestParam Long serviceId,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) Boolean inStock,
        @RequestParam(required = false) Boolean active,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        productService.searchProductsByServiceWithFilters(
            serviceId,
            name,
            category,
            minPrice,
            maxPrice,
            inStock,
            active,
            sortBy,
            sortDir,
            PageRequest.of(page, size)));
    }

    // Inquiry Endpoints
    @PostMapping("/inquiries")
    public ResponseEntity<InquiryDTO> createInquiry(@Valid @RequestBody InquiryDTO inquiryDTO) {
        return ResponseEntity.ok(inquiryService.createInquiry(inquiryDTO));
    }

    @GetMapping("/inquiries/{id}")
    public ResponseEntity<InquiryDTO> getInquiry(@PathVariable Long id) {
        return ResponseEntity.ok(inquiryService.getInquiry(id));
    }

    @GetMapping("/inquiries")
    public ResponseEntity<List<InquiryDTO>> getInquiriesByBusiness(
            @RequestParam Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inquiryService.getInquiriesByBusiness(businessId, PageRequest.of(page, size)));
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/inquiries/{id}/respond")
    public ResponseEntity<InquiryDTO> respondToInquiry(
            @PathVariable Long id,
            @RequestBody InquiryResponseDTO responseDTO) {
        return ResponseEntity.ok(inquiryService.respondToInquiry(id, responseDTO));
    }

    // B2BOrder Endpoints
    @PostMapping("/orders")
    public ResponseEntity<B2BOrderDTO> createOrder(@Valid @RequestBody B2BOrderDTO orderDTO) {
        return ResponseEntity.ok(b2bOrderService.createOrder(orderDTO));
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<B2BOrderDTO>> getOrdersByBusiness(
            @RequestParam Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) B2BOrderStatus status) {
        return ResponseEntity.ok(b2bOrderService.getOrdersByBusiness(businessId, status, PageRequest.of(page, size)));
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<B2BOrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam B2BOrderStatus status) {
        return ResponseEntity.ok(b2bOrderService.updateOrderStatus(id, status));
    }

    // Dashboard Endpoints
    @GetMapping("/dashboard/{businessId}/product-performance")
    public ResponseEntity<List<ProductPerformanceDTO>> getProductPerformance(@PathVariable Long businessId) {
        return ResponseEntity.ok(productService.getProductPerformance(businessId));
    }

    @GetMapping("/dashboard/{businessId}/inquiry-analytics")
    public ResponseEntity<InquiryAnalyticsDTO> getInquiryAnalytics(@PathVariable Long businessId) {
        return ResponseEntity.ok(inquiryService.getInquiryAnalytics(businessId));
    }

    @GetMapping("/dashboard/{businessId}/profile-views")
    public ResponseEntity<ProfileViewsDTO> getProfileViews(@PathVariable Long businessId) {
        return ResponseEntity.ok(businessService.getProfileViews(businessId));
    }

    @PostMapping("/dashboard/{businessId}/bulk-products")
    public ResponseEntity<List<ProductDTO>> bulkUploadProducts(
            @PathVariable Long businessId,
            @Valid @RequestBody BulkProductUploadDTO bulkProductUploadDTO) {
        return ResponseEntity.ok(productService.bulkUploadProducts(businessId, bulkProductUploadDTO));
    }

    @GetMapping("/services/max-products")
    public ResponseEntity<Map<Long, List<ProductDTO>>> getMaxProductsPerService() {
        return ResponseEntity.ok(productService.getMaxProductsPerService());
    }

    @GetMapping("/services/max-products-with-details")
    public ResponseEntity<Map<String, List<ServiceProductsDTO>>> getMaxProductsPerServiceWithDetails() {
        return ResponseEntity.ok(productService.getMaxProductsPerServiceWithDetails());
    }

    @GetMapping("/services/with-products")
    public ResponseEntity<List<ServiceProductsDTO>> getServicesWithProducts() {
        return ResponseEntity.ok(productService.getServicesWithProducts());
    }

    // New Products Endpoint
    /**
     * Get new products (recently added, active and in stock)
     * Example: GET /marketplace/products/new?page=0&size=20
     */
    @GetMapping("/products/new")
    public ResponseEntity<Page<ProductDTO>> getNewProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getNewProducts(page, size));
    }

    // Top Products Endpoint
    /**
     * Get top products (most popular/viewed products)
     * Example: GET /marketplace/products/top?page=0&size=20
     */
    @GetMapping("/products/top")
    public ResponseEntity<Page<ProductDTO>> getTopProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getTopProducts(page, size));
    }
}
