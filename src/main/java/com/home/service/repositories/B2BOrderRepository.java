package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.B2BOrder;
import com.home.service.models.enums.B2BOrderStatus;

public interface B2BOrderRepository extends JpaRepository<B2BOrder, Long> {
    Page<B2BOrder> findBySellerIdAndStatus(Long sellerId, B2BOrderStatus status, Pageable pageable);

    Page<B2BOrder> findBySellerIdOrBuyerId(Long sellerId, Long buyerId, Pageable pageable);
}
