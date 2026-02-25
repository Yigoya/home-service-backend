package com.home.service.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.home.service.dto.BulkProductUploadDTO;
import com.home.service.dto.ProductDTO;
import com.home.service.dto.ProductPerformanceDTO;
import com.home.service.dto.ServiceProductsDTO;
import com.home.service.models.Business;
import com.home.service.models.Product;
import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceCategoryTranslation;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.Services;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.ProductRepository;
import com.home.service.repositories.ServiceRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        logger.info("Creating product: {}", productDTO.getName());
        Business business = businessRepository.findById(productDTO.getBusinessId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Business not found with ID: " + productDTO.getBusinessId()));

        Product product = new Product();
        mapProductDTOToEntity(productDTO, product);
        product.setBusiness(business);

        if (productDTO.getServiceIds() != null) {
            logger.info("Service IDs received from request: {}", productDTO.getServiceIds());
            Set<Services> services = productDTO.getServiceIds().stream()
                    .map(id -> serviceRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + id)))
                    .collect(Collectors.toSet());
            product.setServices(services);
        }

        if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
            logger.info("Images received from request: {}", productDTO.getImages());
            product.setImages(productDTO.getImages());
        }

        Product savedProduct = productRepository.save(product);
        logger.info("Product created with ID: {}", savedProduct.getId());
        return mapProductEntityToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        logger.info("Updating product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));

        if (!product.getBusiness().getId().equals(productDTO.getBusinessId())) {
            throw new ValidationException("Product does not belong to the specified business");
        }

        if (productDTO.getServiceIds() != null) {
            Set<Services> services = productDTO.getServiceIds().stream()
                    .map(sid -> serviceRepository.findById(sid)
                            .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + sid)))
                    .collect(Collectors.toSet());
            product.setServices(services);
        }

        if (productDTO.getImages() == null) {
            productDTO.setImages(product.getImages());
        }

        mapProductDTOToEntity(productDTO, product);
        Product updatedProduct = productRepository.save(product);
        logger.info("Product updated with ID: {}", updatedProduct.getId());
        return mapProductEntityToDTO(updatedProduct);
    }

    public ProductDTO getProduct(Long id) {
        logger.info("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));
        return mapProductEntityToDTO(product);
    }

    public Page<ProductDTO> searchProducts(Pageable pageable, String keyword, String category, Double minPrice,
            Double maxPrice) {
        logger.info("Searching products with keyword: {}, category: {}, minPrice: {}, maxPrice: {}",
                keyword, category, minPrice, maxPrice);

        minPrice = minPrice != null ? minPrice : 0.0;
        maxPrice = maxPrice != null ? maxPrice : Double.MAX_VALUE;
        keyword = keyword != null ? keyword : "";
        category = category != null ? category : "";

        Page<Product> products = productRepository.findByNameContainingAndCategoryAndPriceBetween(
                keyword, category, minPrice, maxPrice, pageable);
        return products.map(this::mapProductEntityToDTO);
    }

    @Transactional
    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));
        // Soft delete to preserve referential integrity with existing orders
        product.setActive(false);
        product.setInStock(false);
        productRepository.save(product);
        logger.info("Product soft-deleted (set inactive) with ID: {}", id);
    }

    public List<ProductPerformanceDTO> getProductPerformance(Long businessId) {
        logger.info("Fetching product performance for business ID: {}", businessId);
    businessRepository.findById(businessId)
        .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));

        List<Product> products = productRepository.findByBusinessId(businessId);
        return products.stream().map(product -> {
            ProductPerformanceDTO dto = new ProductPerformanceDTO();
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setViews(100L); // Mock data, replace with actual analytics
            dto.setInquiries(10L); // Mock data
            dto.setOrders(5L); // Mock data
            dto.setTotalRevenue(5000.0); // Mock data
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<ProductDTO> bulkUploadProducts(Long businessId, BulkProductUploadDTO bulkProductUploadDTO) {
        logger.info("Bulk uploading products for business ID: {}", businessId);
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));

        List<ProductDTO> createdProducts = new ArrayList<>();
        for (ProductDTO productDTO : bulkProductUploadDTO.getProducts()) {
            productDTO.setBusinessId(businessId);
            Product product = new Product();
            mapProductDTOToEntity(productDTO, product);
            product.setBusiness(business);

            if (productDTO.getServiceIds() != null) {
                Set<Services> services = productDTO.getServiceIds().stream()
                        .map(id -> serviceRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + id)))
                        .collect(Collectors.toSet());
                product.setServices(services);
            }

            Product savedProduct = productRepository.save(product);
            createdProducts.add(mapProductEntityToDTO(savedProduct));
        }
        logger.info("Bulk uploaded {} products for business ID: {}", createdProducts.size(), businessId);
        return createdProducts;
    }

    // Created: Get Products by Service
    public Page<ProductDTO> getProductsByService(Long serviceId, Pageable pageable) {
        logger.info("Fetching products for service ID: {}", serviceId);
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + serviceId));

        Page<Product> products = productRepository.findByServices_Id(serviceId, pageable);
        return products.map(this::mapProductEntityToDTO);
    }

    public Page<ProductDTO> searchProductsByServiceAndName(Long serviceId, String name, Pageable pageable) {
        logger.info("Searching products for service ID: {} with name: {}", serviceId, name);
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + serviceId));

        name = name != null ? name : "";
        Page<Product> products = productRepository.findByServices_IdAndNameContainingIgnoreCase(serviceId, name, pageable);
        return products.map(this::mapProductEntityToDTO);
    }

    /**
     * Advanced search for products linked to a specific service with optional filters.
     * Backward compatibility: if only serviceId + name provided behaves like previous method.
     */
    public Page<ProductDTO> searchProductsByServiceWithFilters(
        Long serviceId,
        String name,
        String category,
        Double minPrice,
        Double maxPrice,
        Boolean inStock,
        Boolean active,
        String sortBy,
        String sortDir,
        Pageable pageable) {
    logger.info("Advanced search products for service ID: {}", serviceId);
    serviceRepository.findById(serviceId)
        .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + serviceId));

    // Normalize filters
    String normalizedName = (name == null || name.isBlank()) ? null : name.trim().toLowerCase();
    String namePattern = (normalizedName == null) ? null : "%" + normalizedName + "%"; // lower-cased pattern
    String normalizedCategory = (category == null || category.isBlank()) ? null : category.trim();
    Double min = minPrice;
    Double max = maxPrice;
    Boolean activeFilter = (active == null) ? Boolean.TRUE : active; // default only active

    // Sorting override
    String sortProperty = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
    Sort.Direction direction = (sortDir != null && sortDir.equalsIgnoreCase("asc")) ? Sort.Direction.ASC
        : Sort.Direction.DESC;
    Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, sortProperty));

    Page<Product> products = productRepository.findByServiceWithFilters(
        serviceId,
        namePattern,
        normalizedCategory,
        min,
        max,
        inStock,
        activeFilter,
        sortedPageable);
    return products.map(this::mapProductEntityToDTO);
    }

    // New: Products by Business with filters for app queries
    public Page<ProductDTO> getProductsByBusinessWithFilters(
        Long businessId,
        String search,
        String category,
        Double minPrice,
        Double maxPrice,
        Boolean inStock,
        Boolean active,
        Long serviceId,
        Integer page,
        Integer size,
        String sortBy,
        String sortDir) {
    logger.info("Fetching products for business ID: {} with filters", businessId);

    // Validate business exists
    businessRepository.findById(businessId)
        .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));

    // Normalize inputs
    String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();
    String searchPattern = (normalizedSearch == null) ? null : "%" + normalizedSearch + "%";
    String normalizedCategory = (category == null || category.isBlank()) ? null : category.trim();
    Double min = minPrice;
    Double max = maxPrice;
    // Sorting
    String sortProperty = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
    Sort.Direction direction = (sortDir != null && sortDir.equalsIgnoreCase("asc")) ? Sort.Direction.ASC
        : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 10 : size,
        Sort.by(direction, sortProperty));

    // Default to only active products if not specified
    Boolean activeFilter = (active == null) ? Boolean.TRUE : active;

    Page<Product> products = productRepository.findByBusinessWithFilters(
        businessId,
                searchPattern,
        normalizedCategory,
        min,
        max,
        inStock,
        activeFilter,
        serviceId,
        pageable);
    return products.map(this::mapProductEntityToDTO);
    }

    public Map<Long, List<ProductDTO>> getMaxProductsPerService() {
        logger.info("Fetching up to 5 products per service");
        List<Services> servicesWithProducts = serviceRepository.findServicesWithProducts();
        Map<Long, List<ProductDTO>> result = new HashMap<>();

        for (Services service : servicesWithProducts) {
            Page<Product> products = productRepository.findByServices_Id(
                    service.getId(), PageRequest.of(0, 5));
            if (!products.isEmpty()) {
                result.put(service.getId(), products.getContent().stream()
                        .map(this::mapProductEntityToDTO)
                        .collect(Collectors.toList()));
            }
        }

        return result;
    }

    public Map<String, List<ServiceProductsDTO>> getMaxProductsPerServiceWithDetails() {
        logger.info("Fetching up to 5 products per service with service details");
        List<Services> servicesWithProducts = serviceRepository.findServicesWithProducts();
        Map<String, List<ServiceProductsDTO>> result = new HashMap<>();
        
        // Group services by category
        Map<Long, List<Services>> servicesByCategory = servicesWithProducts.stream()
                .collect(Collectors.groupingBy(service -> service.getCategory().getId()));
        
        for (Map.Entry<Long, List<Services>> entry : servicesByCategory.entrySet()) {
            List<ServiceProductsDTO> serviceProductsList = new ArrayList<>();
            
            for (Services service : entry.getValue()) {
                Page<Product> products = productRepository.findByServices_Id(
                        service.getId(), PageRequest.of(0, 5));
                
                if (!products.isEmpty()) {
                    ServiceProductsDTO serviceProducts = new ServiceProductsDTO();
                    serviceProducts.setId(service.getId());
                    
                    // Get English translation by default
                    ServiceTranslation translation = service.getTranslations().stream()
                            .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                            .findFirst()
                            .orElseGet(() -> service.getTranslations().iterator().next());
                    
                    serviceProducts.setName(translation.getName());
                    serviceProducts.setDescription(translation.getDescription());
                    serviceProducts.setPrice(service.getServiceFee());
                    serviceProducts.setDuration(service.getEstimatedDuration());
                    serviceProducts.setCategoryId(service.getCategory().getId());
                    serviceProducts.setIcon(service.getIcon());
                    
                    List<ProductDTO> productDTOs = products.getContent().stream()
                            .map(this::mapProductEntityToDTO)
                            .collect(Collectors.toList());
                    
                    serviceProducts.setProducts(productDTOs);
                    serviceProductsList.add(serviceProducts);
                }
            }
            
            if (!serviceProductsList.isEmpty()) {
                // Get category name from the first service
                Services firstService = entry.getValue().get(0);
                ServiceCategory category = firstService.getCategory();
                String categoryName = category.getTranslations().stream()
                        .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                        .findFirst()
                        .map(ServiceCategoryTranslation::getName)
                        .orElse("Category " + category.getId());
                
                result.put(categoryName, serviceProductsList);
            }
        }
        
        return result;
    }
    
    public List<ServiceProductsDTO> getServicesWithProducts() {
        logger.info("Fetching services with up to 5 products each");
        List<Services> servicesWithProducts = serviceRepository.findServicesWithProducts();
        List<ServiceProductsDTO> result = new ArrayList<>();
        
        for (Services service : servicesWithProducts) {
            Page<Product> products = productRepository.findByServices_Id(
                    service.getId(), PageRequest.of(0, 5));
            
            if (!products.isEmpty()) {
                ServiceProductsDTO serviceProducts = new ServiceProductsDTO();
                serviceProducts.setId(service.getId());
                
                // Get English translation by default
                ServiceTranslation translation = service.getTranslations().stream()
                        .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                        .findFirst()
                        .orElseGet(() -> service.getTranslations().iterator().next());
                
                serviceProducts.setName(translation.getName());
                serviceProducts.setDescription(translation.getDescription());
                serviceProducts.setPrice(service.getServiceFee());
                serviceProducts.setDuration(service.getEstimatedDuration());
                serviceProducts.setCategoryId(service.getCategory().getId());
                serviceProducts.setIcon(service.getIcon());
                
                List<ProductDTO> productDTOs = products.getContent().stream()
                        .map(this::mapProductEntityToDTO)
                        .collect(Collectors.toList());
                
                serviceProducts.setProducts(productDTOs);
                result.add(serviceProducts);
            }
        }
        
        return result;
    }

    private void mapProductDTOToEntity(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setCurrency(dto.getCurrency());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setMinOrderQuantity(dto.getMinOrderQuantity());
        if (dto.getImages() != null) {
            entity.setImages(dto.getImages());
        }
        entity.setCategory(dto.getCategory());
        entity.setSku(dto.getSku());
        entity.setActive(true);
        entity.setSpecifications(dto.getSpecifications());
        if (dto.getCondition() != null) {
            entity.setCondition(dto.getCondition());
        }
    }

    // Get new products (recently added)
    public Page<ProductDTO> getNewProducts(int page, int size) {
        logger.info("Fetching new products - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findNewProducts(pageable);
        return products.map(this::mapProductEntityToDTO);
    }

    // Get top products (most popular/viewed)
    public Page<ProductDTO> getTopProducts(int page, int size) {
        logger.info("Fetching top products - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findTopProducts(pageable);
        return products.map(this::mapProductEntityToDTO);
    }

    private ProductDTO mapProductEntityToDTO(Product entity) {
        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setCurrency(entity.getCurrency());
        dto.setStockQuantity(entity.getStockQuantity());
        dto.setMinOrderQuantity(entity.getMinOrderQuantity());
        dto.setImages(entity.getImages());
        dto.setCategory(entity.getCategory());
        dto.setSku(entity.getSku());
        dto.setActive(entity.isActive());
        dto.setBusinessId(entity.getBusiness().getId());
        dto.setBusinessName(entity.getBusiness().getName());
        dto.setBusinessEmail(entity.getBusiness().getEmail());
        dto.setBusinessPhoneNumber(entity.getBusiness().getPhoneNumber());
        dto.setBusinessWebsite(entity.getBusiness().getWebsite());
        dto.setBusinessLogo(entity.getBusiness().getLogo());
        dto.setSpecifications(entity.getSpecifications());
        dto.setServiceIds(entity.getServices().stream().map(Services::getId).collect(Collectors.toSet()));
        dto.setCondition(entity.getCondition());
        return dto;
    }
}