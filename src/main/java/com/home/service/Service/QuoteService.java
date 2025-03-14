package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.models.B2BPartner;
import com.home.service.models.Business;
import com.home.service.models.BusinessServices;
import com.home.service.models.Quote;
import com.home.service.models.QuoteItem;
import com.home.service.models.enums.B2BPartnerStatus;
import com.home.service.models.enums.QuoteStatus;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.BusinessServiceRepository;
import com.home.service.repositories.QuoteItemRepository;
import com.home.service.repositories.QuoteRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final BusinessRepository businessRepository;
    private final BusinessServiceRepository businessServiceRepository;
    private final B2BPartnerService b2bPartnerService;

    public QuoteService(QuoteRepository quoteRepository, QuoteItemRepository quoteItemRepository,
            BusinessRepository businessRepository, BusinessServiceRepository businessServiceRepository,
            B2BPartnerService b2bPartnerService) {
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
        this.businessRepository = businessRepository;
        this.businessServiceRepository = businessServiceRepository;
        this.b2bPartnerService = b2bPartnerService;
    }

    @Data
    public static class QuoteItemDTO {
        private Long id;
        private Long serviceId;
        private Integer quantity;
        private Double unitPrice;

        public QuoteItemDTO() {
        }

        public QuoteItemDTO(QuoteItem item) {
            this.id = item.getId();
            this.serviceId = item.getService().getId();
            this.quantity = item.getQuantity();
            this.unitPrice = item.getUnitPrice();
        }
    }

    @Data
    public static class QuoteDTO {
        private Long id;
        private Long partnerId;
        private List<QuoteItemDTO> items = new ArrayList<>();
        private String validUntil;
        private String notes;
        private QuoteStatus status;
        private Double totalAmount;

        public QuoteDTO() {
        }

        public QuoteDTO(Quote quote) {
            this.id = quote.getId();
            this.partnerId = quote.getPartner().getId();
            this.items = quote.getItems().stream()
                    .map(QuoteItemDTO::new)
                    .collect(Collectors.toList());
            this.validUntil = quote.getValidUntil().toString();
            this.notes = quote.getNotes();
            this.status = quote.getStatus();
            this.totalAmount = quote.getTotalAmount();
        }
    }

    @Transactional(readOnly = true)
    public Page<QuoteDTO> getQuotesByBusiness(Long businessId, int page, int size) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Quote> quotes = quoteRepository.findByBusinessId(businessId, pageable);

        return quotes.map(QuoteDTO::new);
    }

    @Transactional
    public QuoteDTO createQuote(Long businessId, QuoteDTO quoteDTO, Long currentUserId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        // if (!business.getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to create quotes for
        // this business");
        // }

        B2BPartner partner = b2bPartnerService.getPartnerById(quoteDTO.getPartnerId());

        // Verify partner belongs to this business
        if (!partner.getBusiness().getId().equals(businessId)) {
            throw new IllegalArgumentException("Partner does not belong to this business");
        }

        // Verify partner is active
        if (partner.getStatus() != B2BPartnerStatus.ACTIVE) {
            throw new IllegalStateException("Cannot create quote with non-active partner");
        }

        // Parse date
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        LocalDate validUntil = LocalDate.parse(quoteDTO.getValidUntil(), formatter);

        // Validate date
        if (validUntil.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Valid until date must be in the future");
        }

        // Validate items
        if (quoteDTO.getItems() == null || quoteDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("Quote must have at least one item");
        }

        Quote quote = new Quote();
        quote.setBusiness(business);
        quote.setPartner(partner);
        quote.setValidUntil(validUntil);
        quote.setNotes(quoteDTO.getNotes());
        quote.setStatus(QuoteStatus.DRAFT);

        // Save quote first to get ID
        Quote savedQuote = quoteRepository.save(quote);

        // Add items
        for (QuoteItemDTO itemDTO : quoteDTO.getItems()) {
            BusinessServices service = businessServiceRepository.findById(itemDTO.getServiceId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found: " + itemDTO.getServiceId()));

            // Verify service belongs to this business
            if (!service.getBusiness().getId().equals(businessId)) {
                throw new IllegalArgumentException(
                        "Service does not belong to this business: " + itemDTO.getServiceId());
            }

            // Validate quantity and price
            if (itemDTO.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }

            if (itemDTO.getUnitPrice() <= 0) {
                throw new IllegalArgumentException("Unit price must be positive");
            }

            QuoteItem item = new QuoteItem();
            item.setQuote(savedQuote);
            item.setService(service);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());

            savedQuote.addItem(item);
        }

        // Calculate total amount
        savedQuote.calculateTotalAmount();

        // Save again with items and total
        Quote finalQuote = quoteRepository.save(savedQuote);
        return new QuoteDTO(finalQuote);
    }

    @Transactional
    public QuoteDTO updateQuote(Long quoteId, QuoteDTO quoteDTO, Long currentUserId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found"));

        // if (!quote.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to update this
        // quote");
        // }

        // Can only update draft quotes
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new IllegalStateException("Only draft quotes can be updated");
        }

        // Parse date
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        LocalDate validUntil = LocalDate.parse(quoteDTO.getValidUntil(), formatter);

        // Validate date
        if (validUntil.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Valid until date must be in the future");
        }

        // Validate items
        if (quoteDTO.getItems() == null || quoteDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("Quote must have at least one item");
        }

        // Update quote fields
        quote.setValidUntil(validUntil);
        quote.setNotes(quoteDTO.getNotes());

        // Clear existing items
        quote.getItems().clear();
        quoteItemRepository.deleteByQuoteId(quoteId);

        // Add new items
        for (QuoteItemDTO itemDTO : quoteDTO.getItems()) {
            BusinessServices service = businessServiceRepository.findById(itemDTO.getServiceId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found: " + itemDTO.getServiceId()));

            // Verify service belongs to this business
            if (!service.getBusiness().getId().equals(quote.getBusiness().getId())) {
                throw new IllegalArgumentException(
                        "Service does not belong to this business: " + itemDTO.getServiceId());
            }

            // Validate quantity and price
            if (itemDTO.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }

            if (itemDTO.getUnitPrice() <= 0) {
                throw new IllegalArgumentException("Unit price must be positive");
            }

            QuoteItem item = new QuoteItem();
            item.setQuote(quote);
            item.setService(service);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());

            quote.addItem(item);
        }

        // Calculate total amount
        quote.calculateTotalAmount();

        Quote updatedQuote = quoteRepository.save(quote);
        return new QuoteDTO(updatedQuote);
    }

    @Transactional
    public QuoteDTO updateQuoteStatus(Long quoteId, QuoteStatus status, Long currentUserId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found"));

        // if (!quote.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to update this quote
        // status");
        // }

        // Validate status transition
        validateStatusTransition(quote.getStatus(), status);

        quote.setStatus(status);
        Quote updatedQuote = quoteRepository.save(quote);
        return new QuoteDTO(updatedQuote);
    }

    private void validateStatusTransition(QuoteStatus currentStatus, QuoteStatus newStatus) {
        if (currentStatus == QuoteStatus.DRAFT &&
                (newStatus == QuoteStatus.ACCEPTED || newStatus == QuoteStatus.REJECTED)) {
            throw new IllegalStateException("Cannot transition from DRAFT to ACCEPTED/REJECTED");
        }

        if (currentStatus == QuoteStatus.SENT && newStatus == QuoteStatus.DRAFT) {
            throw new IllegalStateException("Cannot transition from SENT to DRAFT");
        }

        if ((currentStatus == QuoteStatus.ACCEPTED || currentStatus == QuoteStatus.REJECTED) &&
                (newStatus == QuoteStatus.DRAFT || newStatus == QuoteStatus.SENT)) {
            throw new IllegalStateException("Cannot change status once quote is ACCEPTED/REJECTED");
        }
    }

    @Transactional
    public void deleteQuote(Long quoteId, Long currentUserId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found"));

        // if (!quote.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to delete this
        // quote");
        // }

        // Can only delete draft quotes
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new IllegalStateException("Only draft quotes can be deleted");
        }

        quoteRepository.delete(quote);
    }

    @Transactional(readOnly = true)
    public QuoteDTO getQuoteById(Long quoteId, Long currentUserId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found"));

        // Check if user is authorized to view this quote
        // if (!quote.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to view this quote");
        // }

        return new QuoteDTO(quote);
    }
}