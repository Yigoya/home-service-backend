package com.home.service.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.home.service.models.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
        @Query("SELECT p FROM Product p WHERE p.business.id = :companyId AND " +
                        "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Product> findByCompanyId(Long companyId, String search, Pageable pageable);

        Page<Product> findByNameContainingAndCategoryAndPriceBetween(
                        String keyword, String category, Double minPrice, Double maxPrice, Pageable pageable);

        List<Product> findByBusinessId(Long businessId);
        Page<Product> findByServices_Id(Long serviceId, Pageable pageable);

        Page<Product> findByServices_IdAndNameContainingIgnoreCase(Long serviceId, String name, Pageable pageable);

        // Products by Service with optional filters
        @Query("SELECT DISTINCT p FROM Product p JOIN p.services s WHERE s.id = :serviceId " +
                        "AND (:searchPattern IS NULL OR (LOWER(p.name) LIKE :searchPattern " +
                        "     OR LOWER(p.description) LIKE :searchPattern)) " +
                        "AND (:category IS NULL OR p.category = :category) " +
                        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
                        "AND (:inStock IS NULL OR p.inStock = :inStock) " +
                        "AND (:active IS NULL OR p.isActive = :active)")
        Page<Product> findByServiceWithFilters(Long serviceId,
                        String searchPattern,
                        String category,
                        Double minPrice,
                        Double maxPrice,
                        Boolean inStock,
                        Boolean active,
                        Pageable pageable);

        // Products by Business with optional filters for app queries
        @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.services s WHERE p.business.id = :businessId " +
                        "AND (:searchPattern IS NULL OR (LOWER(p.name) LIKE :searchPattern " +
                        "     OR LOWER(p.description) LIKE :searchPattern)) " +
                        "AND (:category IS NULL OR p.category = :category) " +
                        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
                        "AND (:inStock IS NULL OR p.inStock = :inStock) " +
                        "AND (:active IS NULL OR p.isActive = :active) " +
                        "AND (:serviceId IS NULL OR s.id = :serviceId)")
        Page<Product> findByBusinessWithFilters(Long businessId,
                        String searchPattern,
                        String category,
                        Double minPrice,
                        Double maxPrice,
                        Boolean inStock,
                        Boolean active,
                        Long serviceId,
                        Pageable pageable);

        // Get new products (recently created, active and in stock)
        @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.inStock = true " +
                        "ORDER BY p.createdAt DESC")
        Page<Product> findNewProducts(Pageable pageable);

        // Get top products (based on views/orders - using mock logic for now, sorted by id desc as proxy)
        // TODO: Replace with actual analytics when available (views, orders, revenue)
        @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.inStock = true " +
                        "ORDER BY p.id DESC")
        Page<Product> findTopProducts(Pageable pageable);
}