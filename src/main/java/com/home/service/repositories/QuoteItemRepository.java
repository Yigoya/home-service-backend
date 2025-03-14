package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.QuoteItem;

import java.util.List;

@Repository
public interface QuoteItemRepository extends JpaRepository<QuoteItem, Long> {
    List<QuoteItem> findByQuoteId(Long quoteId);

    void deleteByQuoteId(Long quoteId);
}