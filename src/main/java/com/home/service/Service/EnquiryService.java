package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.models.Business;
import com.home.service.models.Enquiry;
import com.home.service.models.User;
import com.home.service.models.enums.EnquiryStatus;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.EnquiryRepository;
import com.home.service.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@Service
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    public EnquiryService(EnquiryRepository enquiryRepository,
            BusinessRepository businessRepository,
            UserRepository userRepository) {
        this.enquiryRepository = enquiryRepository;
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
    }

    public static class EnquiryDTO {
        public Long businessId;
        public String name;
        public String email;
        public String phoneNumber;
        public String message;

        public EnquiryDTO() {
        }

        public EnquiryDTO(Enquiry enquiry) {
            this.businessId = enquiry.getBusiness().getId();
            this.name = enquiry.getName();
            this.email = enquiry.getEmail();
            this.phoneNumber = enquiry.getPhoneNumber();
            this.message = enquiry.getMessage();
        }
    }

    public EnquiryDTO createEnquiry(@Valid EnquiryDTO dto, Long currentUserId) {
        Business business = businessRepository.findById(dto.businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + dto.businessId));

        Enquiry enquiry = new Enquiry();
        enquiry.setBusiness(business);
        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + currentUserId));
            enquiry.setUser(user);
        }
        enquiry.setName(dto.name);
        enquiry.setEmail(dto.email);
        enquiry.setPhoneNumber(dto.phoneNumber);
        enquiry.setMessage(dto.message);
        enquiry.setStatus(EnquiryStatus.PENDING);

        Enquiry savedEnquiry = enquiryRepository.save(enquiry);
        return new EnquiryDTO(savedEnquiry);
    }

    public Enquiry getEnquiryById(Long id, Long currentUserId) {
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enquiry not found with ID: " + id));
        // if (!canAccessEnquiry(enquiry, currentUserId)) {
        // throw new AccessDeniedException("You do not have permission to view this
        // enquiry");
        // }
        return enquiry;
    }

    public EnquiryDTO updateEnquiryStatus(Long id, EnquiryStatus status, Long currentUserId) {
        Enquiry enquiry = getEnquiryById(id, currentUserId);
        // if (!businessRepository.findById(enquiry.getBusiness().getId())
        // .map(b -> b.getOwner().getId().equals(currentUserId))
        // .orElse(false)) {
        // throw new AccessDeniedException("Only the business owner can update enquiry
        // status");
        // }
        enquiry.setStatus(status);
        Enquiry updatedEnquiry = enquiryRepository.save(enquiry);
        return new EnquiryDTO(updatedEnquiry);
    }

    public Page<EnquiryDTO> getEnquiriesByBusiness(Long businessId, int page, int size, Long currentUserId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));
        // if (!business.getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("Only the business owner can view
        // enquiries");
        // }
        Pageable pageable = PageRequest.of(page, size);
        Page<Enquiry> enquiries = enquiryRepository.findByBusiness(business, pageable);
        return enquiries.map(EnquiryDTO::new);
    }

    public Page<EnquiryDTO> getEnquiriesByUser(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        Pageable pageable = PageRequest.of(page, size);
        Page<Enquiry> enquiries = enquiryRepository.findByUser(user, pageable);
        return enquiries.map(EnquiryDTO::new);
    }

    private boolean canAccessEnquiry(Enquiry enquiry, Long currentUserId) {
        return (enquiry.getUser() != null && enquiry.getUser().getId().equals(currentUserId)) ||
                businessRepository.findById(enquiry.getBusiness().getId())
                        .map(b -> b.getOwner().getId().equals(currentUserId))
                        .orElse(false);
    }
}