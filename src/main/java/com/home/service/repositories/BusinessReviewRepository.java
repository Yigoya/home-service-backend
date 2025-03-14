package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.home.service.models.Business;
import com.home.service.models.BusinessReview;
import com.home.service.models.User;

public interface BusinessReviewRepository
        extends JpaRepository<BusinessReview, Long>, JpaSpecificationExecutor<BusinessReview> {
    Page<BusinessReview> findByBusiness(Business business, Pageable pageable);

    Page<BusinessReview> findByUser(User user, Pageable pageable);
}