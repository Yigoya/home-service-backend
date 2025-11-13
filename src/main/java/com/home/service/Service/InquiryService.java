package com.home.service.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.home.service.dto.InquiryAnalyticsDTO;
import com.home.service.dto.InquiryDTO;
import com.home.service.dto.InquiryResponseDTO;
import com.home.service.models.Business;
import com.home.service.models.Inquiry;
import com.home.service.models.Product;
import com.home.service.models.enums.InquiryStatus;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.InquiryRepository;
import com.home.service.repositories.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class InquiryService {

    private static final Logger logger = LoggerFactory.getLogger(InquiryService.class);

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public InquiryDTO createInquiry(InquiryDTO inquiryDTO) {
        logger.info("Creating inquiry with subject: {}", inquiryDTO.getSubject());
        Business sender = businessRepository.findById(inquiryDTO.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Sender business not found with ID: " + inquiryDTO.getSenderId()));
        Business recipient = businessRepository.findById(inquiryDTO.getRecipientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recipient business not found with ID: " + inquiryDTO.getRecipientId()));

        Product product = null;
        if (inquiryDTO.getProductId() != null) {
            product = productRepository.findById(inquiryDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Product not found with ID: " + inquiryDTO.getProductId()));
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setSubject(inquiryDTO.getSubject());
        inquiry.setMessage(inquiryDTO.getMessage());
        inquiry.setSender(sender);
        inquiry.setRecipient(recipient);
        inquiry.setProduct(product);
        inquiry.setStatus(InquiryStatus.PENDING);

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        logger.info("Inquiry created with ID: {}", savedInquiry.getId());
        return mapInquiryEntityToDTO(savedInquiry);
    }

    public InquiryDTO getInquiry(Long id) {
        logger.info("Fetching inquiry with ID: {}", id);
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inquiry not found with ID: " + id));
        return mapInquiryEntityToDTO(inquiry);
    }

    public List<InquiryDTO> getInquiriesByBusiness(Long businessId, Pageable pageable) {
        logger.info("Fetching inquiries for business ID: {}", businessId);
        businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));

        Page<Inquiry> inquiries = inquiryRepository.findBySenderIdOrRecipientId(businessId, businessId, pageable);
        return inquiries.getContent().stream()
                .map(this::mapInquiryEntityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InquiryDTO respondToInquiry(Long id, InquiryResponseDTO responseDTO) {
        logger.info("Responding to inquiry with ID: {}", id);
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inquiry not found with ID: " + id));

        inquiry.setStatus(responseDTO.getStatus());
        inquiry.setRespondedAt(LocalDateTime.now());
        // Note: Append response message to existing message or store separately
        inquiry.setMessage(inquiry.getMessage() + "\nResponse: " + responseDTO.getResponseMessage());

        Inquiry updatedInquiry = inquiryRepository.save(inquiry);
        logger.info("Inquiry responded with ID: {}", updatedInquiry.getId());
        return mapInquiryEntityToDTO(updatedInquiry);
    }

    public InquiryAnalyticsDTO getInquiryAnalytics(Long businessId) {
        logger.info("Fetching inquiry analytics for business ID: {}", businessId);
        businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));

        List<Inquiry> inquiries = inquiryRepository.findBySenderIdOrRecipientId(businessId, businessId);
        InquiryAnalyticsDTO analytics = new InquiryAnalyticsDTO();
        analytics.setTotalInquiries((long) inquiries.size());
        analytics.setPendingInquiries(inquiries.stream()
                .filter(i -> i.getStatus() == InquiryStatus.PENDING)
                .count());
        analytics.setRespondedInquiries(inquiries.stream()
                .filter(i -> i.getStatus() == InquiryStatus.RESPONDED)
                .count());
        analytics.setClosedInquiries(inquiries.stream()
                .filter(i -> i.getStatus() == InquiryStatus.CLOSED)
                .count());
        return analytics;
    }

    private InquiryDTO mapInquiryEntityToDTO(Inquiry entity) {
        InquiryDTO dto = new InquiryDTO();
        dto.setId(entity.getId());
        dto.setSubject(entity.getSubject());
        dto.setMessage(entity.getMessage());
        dto.setSenderId(entity.getSender().getId());
        dto.setRecipientId(entity.getRecipient().getId());
        dto.setProductId(entity.getProduct() != null ? entity.getProduct().getId() : null);
        dto.setStatus(entity.getStatus());
        dto.setRespondedAt(entity.getRespondedAt());
        return dto;
    }
}