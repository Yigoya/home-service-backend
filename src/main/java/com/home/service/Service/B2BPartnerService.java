package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.Service.BusinessLocationService.BusinessLocationDTO;
import com.home.service.models.B2BPartner;
import com.home.service.models.Business;
import com.home.service.models.enums.B2BPartnerStatus;
import com.home.service.models.enums.B2BPartnerType;
import com.home.service.repositories.B2BPartnerRepository;
import com.home.service.repositories.BusinessRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.Data;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class B2BPartnerService {

    private final B2BPartnerRepository b2bPartnerRepository;
    private final BusinessRepository businessRepository;

    public B2BPartnerService(B2BPartnerRepository b2bPartnerRepository, BusinessRepository businessRepository) {
        this.b2bPartnerRepository = b2bPartnerRepository;
        this.businessRepository = businessRepository;
    }

    @Data
    public static class B2BPartnerDTO {
        private Long id;
        private Long partnerBusinessId;
        private String partnerBusinessName;
        private String partnerBusinessEmail;
        private String partnerBusinessPhone;
        private BusinessLocationDTO partnerLocation;
        private B2BPartnerType partnerType;
        private Double creditLimit;
        private String paymentTerms;
        private B2BPartnerStatus status;
        private String createdAt;
        private String updatedAt;

        public B2BPartnerDTO() {
        }

        public B2BPartnerDTO(B2BPartner partner) {
            this.id = partner.getId();
            this.partnerBusinessId = partner.getPartnerBusiness().getId();
            this.partnerBusinessName = partner.getPartnerBusiness().getName();
            this.partnerBusinessEmail = partner.getPartnerBusiness().getEmail();
            this.partnerBusinessPhone = partner.getPartnerBusiness().getPhoneNumber();
            this.partnerLocation = new BusinessLocationDTO(partner.getPartnerBusiness().getLocation());
            this.partnerType = partner.getPartnerType();
            this.creditLimit = partner.getCreditLimit();
            this.paymentTerms = partner.getPaymentTerms();
            this.status = partner.getStatus();
            this.createdAt = partner.getCreatedAt().toString();
            this.updatedAt = partner.getUpdatedAt().toString();
        }
    }

    @Data
    public static class PartnerRequestDTO {
        private Long id;
        private Long requestingBusinessId;
        private String requestingBusinessName;
        private String requestingBusinessEmail;
        private String requestingBusinessPhone;
        private BusinessLocationDTO requestingLocation;
        private B2BPartnerType partnerType;
        private Double creditLimit;
        private String paymentTerms;
        private B2BPartnerStatus status;
        private String createdAt;
        private String updatedAt;

        public PartnerRequestDTO() {
        }

        public PartnerRequestDTO(B2BPartner partner) {
            this.id = partner.getId();
            this.requestingBusinessId = partner.getBusiness().getId();
            this.requestingBusinessName = partner.getBusiness().getName();
            this.requestingBusinessEmail = partner.getBusiness().getEmail();
            this.requestingBusinessPhone = partner.getBusiness().getPhoneNumber();
            this.requestingLocation = new BusinessLocationDTO(partner.getBusiness().getLocation());
            this.partnerType = partner.getPartnerType();
            this.creditLimit = partner.getCreditLimit();
            this.paymentTerms = partner.getPaymentTerms();
            this.status = partner.getStatus();
            this.createdAt = partner.getCreatedAt().toString();
            this.updatedAt = partner.getUpdatedAt().toString();
        }
    }

    @Data
    public static class BusinessPartnersResponse {
        private Page<B2BPartnerDTO> partners;
        private Page<PartnerRequestDTO> partnerRequests;

        public BusinessPartnersResponse(Page<B2BPartnerDTO> partners, Page<PartnerRequestDTO> partnerRequests) {
            this.partners = partners;
            this.partnerRequests = partnerRequests;
        }
    }

    @Transactional(readOnly = true)
    public Page<B2BPartnerDTO> getPartnersByBusiness(Long businessId, int page, int size) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<B2BPartner> partners = b2bPartnerRepository.findByBusinessId(businessId, pageable);

        return partners.map(B2BPartnerDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<PartnerRequestDTO> getPartnerRequestsByBusiness(Long businessId, int page, int size) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<B2BPartner> partnerRequests = b2bPartnerRepository.findByPartnerBusinessId(businessId, pageable);

        return partnerRequests.map(PartnerRequestDTO::new);
    }

    @Transactional(readOnly = true)
    public BusinessPartnersResponse getAllBusinessPartners(Long businessId, int page, int size) {
        Page<B2BPartnerDTO> partners = getPartnersByBusiness(businessId, page, size);
        Page<PartnerRequestDTO> partnerRequests = getPartnerRequestsByBusiness(businessId, page, size);

        return new BusinessPartnersResponse(partners, partnerRequests);
    }

    @Transactional
    public B2BPartnerDTO createPartner(Long businessId, B2BPartnerDTO partnerDTO, Long currentUserId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        // if (!business.getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to add partners to
        // this business");
        // }

        Business partnerBusiness = businessRepository.findById(partnerDTO.getPartnerBusinessId())
                .orElseThrow(() -> new EntityNotFoundException("Partner business not found"));

        // Check if partnership already exists
        b2bPartnerRepository.findByBusinessIdAndPartnerBusinessId(businessId, partnerDTO.getPartnerBusinessId())
                .ifPresent(p -> {
                    throw new IllegalStateException("Partnership already exists with this business");
                });

        // Validate credit limit
        if (partnerDTO.getCreditLimit() != null && partnerDTO.getCreditLimit() <= 0) {
            throw new IllegalArgumentException("Credit limit must be a positive number");
        }

        B2BPartner partner = new B2BPartner();
        partner.setBusiness(business);
        partner.setPartnerBusiness(partnerBusiness);
        partner.setPartnerType(partnerDTO.getPartnerType());
        partner.setCreditLimit(partnerDTO.getCreditLimit());
        partner.setPaymentTerms(partnerDTO.getPaymentTerms());
        partner.setStatus(B2BPartnerStatus.PENDING);

        B2BPartner savedPartner = b2bPartnerRepository.save(partner);
        return new B2BPartnerDTO(savedPartner);
    }

    @Transactional
    public B2BPartnerDTO updatePartner(Long partnerId, B2BPartnerDTO partnerDTO, Long currentUserId) {
        B2BPartner partner = b2bPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Partner not found"));

        // if (!partner.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to update this
        // partnership");
        // }

        // Validate credit limit
        if (partnerDTO.getCreditLimit() != null && partnerDTO.getCreditLimit() <= 0) {
            throw new IllegalArgumentException("Credit limit must be a positive number");
        }

        // Cannot change partner business
        if (!partner.getPartnerBusiness().getId().equals(partnerDTO.getPartnerBusinessId())) {
            throw new IllegalArgumentException("Cannot change partner business");
        }

        partner.setPartnerType(partnerDTO.getPartnerType());
        partner.setCreditLimit(partnerDTO.getCreditLimit());
        partner.setPaymentTerms(partnerDTO.getPaymentTerms());

        B2BPartner updatedPartner = b2bPartnerRepository.save(partner);
        return new B2BPartnerDTO(updatedPartner);
    }

    @Transactional
    public B2BPartnerDTO updatePartnerStatus(Long partnerId, B2BPartnerStatus status, Long currentUserId) {
        B2BPartner partner = b2bPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Partner not found"));

        // if (!partner.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to update this
        // partnership status");
        // }

        // Validate status transition
        validateStatusTransition(partner.getStatus(), status);

        partner.setStatus(status);
        B2BPartner updatedPartner = b2bPartnerRepository.save(partner);
        return new B2BPartnerDTO(updatedPartner);
    }

    private void validateStatusTransition(B2BPartnerStatus currentStatus, B2BPartnerStatus newStatus) {
        if (currentStatus == B2BPartnerStatus.PENDING && newStatus == B2BPartnerStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot transition from PENDING to SUSPENDED");
        }
    }

    @Transactional
    public void deletePartner(Long partnerId, Long currentUserId) {
        B2BPartner partner = b2bPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Partner not found"));

        // if (!partner.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to delete this
        // partnership");
        // }

        b2bPartnerRepository.delete(partner);
    }

    @Transactional(readOnly = true)
    public B2BPartner getPartnerById(Long partnerId) {
        return b2bPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Partner not found"));
    }

    @Transactional(readOnly = true)
    public boolean isActivePartner(Long partnerId) {
        B2BPartner partner = getPartnerById(partnerId);
        return partner.getStatus() == B2BPartnerStatus.ACTIVE;
    }
}