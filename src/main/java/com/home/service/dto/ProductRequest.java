package com.home.service.dto;

import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.home.service.models.enums.ProductCondition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ProductRequest {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code (e.g., ETB, USD)")
    private String currency;

    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @PositiveOrZero(message = "Minimum order quantity cannot be negative")
    private Integer minOrderQuantity;
    private MultipartFile[] images;
    private String category;
    private String sku;
    private boolean isActive;

    @NotNull(message = "Business ID is required")
    private Long businessId;

    private String specifications;
    private String serviceIdsJson;
    private String condition; // NEW or USED

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public Set<Long> getServiceIds() {
        log.debug("Service IDs JSON: {}", serviceIdsJson);
        if (serviceIdsJson == null || serviceIdsJson.isEmpty()) {
            return null;
        }
        try {
            log.debug("Parsing service IDs JSON: {}", serviceIdsJson);
            return mapper.readValue(serviceIdsJson,
                    mapper.getTypeFactory().constructCollectionType(Set.class, Long.class));
        } catch (JsonProcessingException e) {
            log.error("Error parsing service IDs JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid service IDs format: " + e.getMessage(), e);
        }
    }

    /**
     * Convert this request to a ProductDTO
     * Note: This doesn't include the image paths as they need to be processed
     * separately
     */
    public ProductDTO toProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setDescription(this.description);
        dto.setPrice(this.price);
        dto.setCurrency(this.currency);
        dto.setStockQuantity(this.stockQuantity);
        dto.setMinOrderQuantity(this.minOrderQuantity);
        dto.setCategory(this.category);
        dto.setSku(this.sku);
        dto.setActive(this.isActive);
        dto.setBusinessId(this.businessId);
        dto.setSpecifications(this.specifications);
        dto.setServiceIds(this.getServiceIds());
        if (this.condition != null && !this.condition.isBlank()) {
            dto.setCondition(ProductCondition.valueOf(this.condition.trim().toUpperCase()));
        }
        return dto;
    }

    @jakarta.validation.constraints.AssertTrue(message = "Minimum order quantity cannot exceed stock quantity")
    public boolean isMinOrderWithinStock() {
        if (minOrderQuantity == null || stockQuantity == null) {
            return true;
        }
        return minOrderQuantity <= stockQuantity;
    }
}
