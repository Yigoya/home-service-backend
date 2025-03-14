package com.home.service.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.models.Business;
import com.home.service.models.Partnership;
import com.home.service.models.enums.PartnershipStatus;
import com.home.service.models.enums.PartnershipType;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.PartnershipRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PartnershipService {

    private final PartnershipRepository partnershipRepository;
    private final BusinessRepository businessRepository;

    public PartnershipService(PartnershipRepository partnershipRepository, BusinessRepository businessRepository) {
        this.partnershipRepository = partnershipRepository;
        this.businessRepository = businessRepository;
    }

    public static class PartnershipDTO {
        public String partnerName;
        public String partnerLogo;
        public String partnerWebsite;
        public PartnershipType partnershipType;
        public String startDate;
        public String endDate;
        public PartnershipStatus status;
        public String description;
        public String contactPerson;
        public String contactEmail;
        public String contactPhone;
        public List<String> agreements;
        public List<String> benefits;
    }

    public Page<Partnership> getPartnerships(Long companyId, int page, int size, String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        Pageable pageable = PageRequest.of(page, size);
        return partnershipRepository.findByCompanyId(companyId, pageable);
    }

    public Partnership createPartnership(Long companyId, PartnershipDTO dto, String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        Partnership partnership = new Partnership();
        populatePartnership(partnership, dto, company);
        return partnershipRepository.save(partnership);
    }

    public Partnership updatePartnership(Long companyId, Long partnershipId, PartnershipDTO dto, Long currentUserId) {
        Partnership partnership = partnershipRepository.findById(partnershipId)
                .orElseThrow(() -> new EntityNotFoundException("Partnership not found"));
        if (!partnership.getCompany().getId().equals(companyId)
                || !partnership.getCompany().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        populatePartnership(partnership, dto, partnership.getCompany());
        return partnershipRepository.save(partnership);
    }

    public void deletePartnership(Long companyId, Long partnershipId, Long currentUserId) {
        Partnership partnership = partnershipRepository.findById(partnershipId)
                .orElseThrow(() -> new EntityNotFoundException("Partnership not found"));
        if (!partnership.getCompany().getId().equals(companyId)
                || !partnership.getCompany().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        partnershipRepository.delete(partnership);
    }

    private void populatePartnership(Partnership partnership, PartnershipDTO dto, Business company) {
        partnership.setPartnerName(dto.partnerName);
        partnership.setPartnerLogo(dto.partnerLogo);
        partnership.setPartnerWebsite(dto.partnerWebsite);
        partnership.setPartnershipType(dto.partnershipType);
        partnership.setStartDate(dto.startDate);
        partnership.setEndDate(dto.endDate);
        partnership.setStatus(dto.status);
        partnership.setDescription(dto.description);
        partnership.setContactPerson(dto.contactPerson);
        partnership.setContactEmail(dto.contactEmail);
        partnership.setContactPhone(dto.contactPhone);
        partnership.setAgreements(dto.agreements);
        partnership.setBenefits(dto.benefits);
        partnership.setCompany(company);
    }
}