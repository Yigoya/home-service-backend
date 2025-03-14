package com.home.service.Service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.models.Business;
import com.home.service.repositories.BusinessRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReportService {

    private final BusinessRepository businessRepository;

    public ReportService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    public byte[] generateReport(Long companyId, String type, String format, String start, String end,
            String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        // Dummy implementation (replace with real report generation)
        return "Sample report content".getBytes();
    }
}