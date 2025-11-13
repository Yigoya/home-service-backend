package com.home.service.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.Business;
import com.home.service.models.BusinessPromotion;
import com.home.service.models.enums.PromotionType;
import com.home.service.models.enums.BusinessType;

public interface BusinessPromotionRepository extends JpaRepository<BusinessPromotion, Long> {
    Page<BusinessPromotion> findByBusiness(Business business, Pageable pageable);

    @Query("SELECT p FROM BusinessPromotion p WHERE p.startDate <= :now AND p.endDate >= :now")
    Page<BusinessPromotion> findByStartDateBeforeAndEndDateAfter(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT p FROM BusinessPromotion p WHERE p.startDate <= :now AND p.endDate >= :now AND p.type = :type ORDER BY p.createdAt DESC")
    Page<BusinessPromotion> findActivePromotionsByType(@Param("now") LocalDateTime now, @Param("type") PromotionType type, Pageable pageable);

    @Query("SELECT p FROM BusinessPromotion p WHERE p.startDate <= :now AND p.endDate >= :now AND p.business.industry = :industry ORDER BY p.createdAt DESC")
    Page<BusinessPromotion> findActivePromotionsByIndustry(@Param("now") LocalDateTime now, @Param("industry") String industry, Pageable pageable);

    @Query("SELECT p FROM BusinessPromotion p WHERE p.startDate <= :now AND p.endDate >= :now AND p.business.businessType = :businessType ORDER BY p.createdAt DESC")
    Page<BusinessPromotion> findActivePromotionsByBusinessType(@Param("now") LocalDateTime now, @Param("businessType") BusinessType businessType, Pageable pageable);

    @Query("SELECT p FROM BusinessPromotion p WHERE p.startDate <= :now AND p.endDate >= :now AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.business.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.business.industry) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY p.createdAt DESC")
    Page<BusinessPromotion> findActivePromotionsBySearch(@Param("now") LocalDateTime now, @Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM BusinessPromotion p WHERE p.startDate <= :now AND p.endDate >= :now AND p.isFeatured = true ORDER BY p.createdAt DESC")
    Page<BusinessPromotion> findFeaturedPromotions(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT DISTINCT p FROM BusinessPromotion p " +
           "JOIN p.business b " +
           "JOIN b.services s " +
           "WHERE p.startDate <= :now AND p.endDate >= :now AND s.id = :serviceId " +
           "ORDER BY p.createdAt DESC")
    Page<BusinessPromotion> findActivePromotionsByServiceId(@Param("now") LocalDateTime now, @Param("serviceId") Long serviceId, Pageable pageable);
}