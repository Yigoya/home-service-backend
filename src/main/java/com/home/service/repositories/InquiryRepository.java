package com.home.service.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Inquiry;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findBySenderIdOrRecipientId(Long senderId, Long recipientId, Pageable pageable);

    List<Inquiry> findBySenderIdOrRecipientId(Long senderId, Long recipientId);
}
