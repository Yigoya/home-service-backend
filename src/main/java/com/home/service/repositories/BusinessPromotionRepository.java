package com.home.service.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.Business;
import com.home.service.models.BusinessPromotion;

public interface BusinessPromotionRepository extends JpaRepository<BusinessPromotion, Long> {
    Page<BusinessPromotion> findByBusiness(Business business, Pageable pageable);

    @Query("SELECT p FROM BusinessPromotion p WHERE p.startDate <= :now AND p.endDate >= :now")
    Page<BusinessPromotion> findByStartDateBeforeAndEndDateAfter(@Param("now") LocalDateTime now, Pageable pageable);
}