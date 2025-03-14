package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Business;
import com.home.service.models.Enquiry;
import com.home.service.models.User;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {
    Page<Enquiry> findByBusiness(Business business, Pageable pageable);

    Page<Enquiry> findByUser(User user, Pageable pageable);
}