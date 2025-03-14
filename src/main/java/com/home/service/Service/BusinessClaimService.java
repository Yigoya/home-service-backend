package com.home.service.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.models.User;
import com.home.service.models.enums.ClaimStatus;
import com.home.service.models.enums.UserRole;
import com.home.service.models.Business;
import com.home.service.models.BusinessClaim;
import com.home.service.repositories.BusinessClaimRepository;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@Service
public class BusinessClaimService {

    private final BusinessClaimRepository businessClaimRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    public BusinessClaimService(BusinessClaimRepository businessClaimRepository,
            BusinessRepository businessRepository,
            UserRepository userRepository) {
        this.businessClaimRepository = businessClaimRepository;
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
    }

    public static class BusinessClaimDTO {
        public Long businessId;
        public List<String> proofDocuments;

        public BusinessClaimDTO() {
        }

        public BusinessClaimDTO(BusinessClaim claim) {
            this.businessId = claim.getBusiness().getId();
            this.proofDocuments = claim.getProofDocuments();
        }
    }

    public BusinessClaimDTO createClaim(@Valid BusinessClaimDTO dto, Long currentUserId) {
        Business business = businessRepository.findById(dto.businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + dto.businessId));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + currentUserId));

        BusinessClaim claim = new BusinessClaim();
        claim.setBusiness(business);
        claim.setUser(user);
        claim.setStatus(ClaimStatus.PENDING);
        claim.setProofDocuments(dto.proofDocuments);

        BusinessClaim savedClaim = businessClaimRepository.save(claim);
        return new BusinessClaimDTO(savedClaim);
    }

    public BusinessClaim getClaimById(Long id, Long currentUserId) {
        BusinessClaim claim = businessClaimRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found with ID: " + id));
        if (!claim.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to view this claim");
        }
        return claim;
    }

    public BusinessClaimDTO updateClaimStatus(Long id, ClaimStatus status, Long currentUserId) {
        BusinessClaim claim = businessClaimRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found with ID: " + id));
        // Assume only admins can update claim status
        if (!userRepository.findById(currentUserId).map(User::getRole).orElse(null).equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("Only admins can update claim status");
        }
        claim.setStatus(status);
        if (status == ClaimStatus.APPROVED) {
            Business business = claim.getBusiness();
            business.setOwner(claim.getUser());
            businessRepository.save(business);
        }
        BusinessClaim updatedClaim = businessClaimRepository.save(claim);
        return new BusinessClaimDTO(updatedClaim);
    }

    public List<BusinessClaimDTO> getClaimsByBusiness(Long businessId, Long currentUserId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));
        if (!business.getOwner().getId().equals(currentUserId) &&
                !userRepository.findById(currentUserId).map(User::getRole).orElse(null).equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("Only the business owner or admin can view claims");
        }
        List<BusinessClaim> claims = businessClaimRepository.findByBusiness(business);
        return claims.stream().map(BusinessClaimDTO::new).toList();
    }

    public Page<BusinessClaimDTO> getClaimsByUser(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessClaim> claimsPage = businessClaimRepository.findByUser(user, pageable);
        return claimsPage.map(BusinessClaimDTO::new);
    }
}