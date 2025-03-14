package com.home.service.Service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.models.Business;
import com.home.service.models.Product;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.ProductRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;

    public ProductService(ProductRepository productRepository, BusinessRepository businessRepository) {
        this.productRepository = productRepository;
        this.businessRepository = businessRepository;
    }

    public static class ProductDTO {
        public String name;
        public String description;
        public double price;
        public String category;
        public String image;
        public boolean inStock;
        public String sku;
        public int inventory;
        public int minimumOrderQuantity;
        public int leadTime;
        public String unitOfMeasure;
        public Map<String, String> specifications;
        public List<String> certifications;

        public ProductDTO() {
        }

        public ProductDTO(Product product) {
            this.name = product.getName();
            this.description = product.getDescription();
            this.price = product.getPrice();
            this.category = product.getCategory();
            this.image = product.getImage();
            this.inStock = product.isInStock();
            this.sku = product.getSku();
            this.inventory = product.getInventory();
            this.minimumOrderQuantity = product.getMinimumOrderQuantity();
            this.leadTime = product.getLeadTime();
            this.unitOfMeasure = product.getUnitOfMeasure();
            this.specifications = product.getSpecifications();
            this.certifications = product.getCertifications();
        }
    }

    public Page<ProductDTO> getProducts(Long companyId, int page, int size, String sort, String search,
            Long currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        Sort sorting = Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Product> productPage = productRepository.findByCompanyId(companyId, search, pageable);
        return productPage.map(ProductDTO::new);
    }

    public ProductDTO createProduct(Long companyId, ProductDTO dto, Long currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        Product product = new Product();
        populateProduct(product, dto, company);
        Product savedProduct = productRepository.save(product);
        return new ProductDTO(savedProduct);
    }

    public ProductDTO updateProduct(Long companyId, Long productId, ProductDTO dto, String currentUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (!product.getCompany().getId().equals(companyId)
                || !product.getCompany().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        populateProduct(product, dto, product.getCompany());
        Product savedProduct = productRepository.save(product);
        return new ProductDTO(savedProduct);
    }

    public void deleteProduct(Long companyId, Long productId, String currentUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (!product.getCompany().getId().equals(companyId)
                || !product.getCompany().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        productRepository.delete(product);
    }

    private void populateProduct(Product product, ProductDTO dto, Business company) {
        product.setName(dto.name);
        product.setDescription(dto.description);
        product.setPrice(dto.price);
        product.setCategory(dto.category);
        product.setImage(dto.image);
        product.setInStock(dto.inStock);
        product.setSku(dto.sku);
        product.setInventory(dto.inventory);
        product.setMinimumOrderQuantity(dto.minimumOrderQuantity);
        product.setLeadTime(dto.leadTime);
        product.setUnitOfMeasure(dto.unitOfMeasure);
        product.setSpecifications(dto.specifications);
        product.setCertifications(dto.certifications);
        product.setCompany(company);
    }
}