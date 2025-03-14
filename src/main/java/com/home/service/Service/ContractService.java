package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.models.B2BPartner;
import com.home.service.models.Business;
import com.home.service.models.Contract;
import com.home.service.models.enums.B2BPartnerStatus;
import com.home.service.models.enums.ContractStatus;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.ContractRepository;
import com.home.service.controller.B2BController.ContractRequest;

import jakarta.persistence.EntityNotFoundException;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final BusinessRepository businessRepository;
    private final B2BPartnerService b2bPartnerService;
    private final String UPLOAD_DIR = "uploads/contracts/";

    public ContractService(ContractRepository contractRepository, BusinessRepository businessRepository,
            B2BPartnerService b2bPartnerService) {
        this.contractRepository = contractRepository;
        this.businessRepository = businessRepository;
        this.b2bPartnerService = b2bPartnerService;

        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Data
    public static class ContractDTO {
        private Long id;
        private Long partnerId;
        private String title;
        private String description;
        private String startDate;
        private String endDate;
        private String terms;
        private List<String> documents;
        private ContractStatus status;

        public ContractDTO() {
        }

        public ContractDTO(Contract contract) {
            this.id = contract.getId();
            this.partnerId = contract.getPartner().getId();
            this.title = contract.getTitle();
            this.description = contract.getDescription();
            this.startDate = contract.getStartDate().toString();
            this.endDate = contract.getEndDate().toString();
            this.terms = contract.getTerms();
            this.documents = contract.getDocuments();
            this.status = contract.getStatus();
        }
    }

    @Transactional(readOnly = true)
    public Page<ContractDTO> getContractsByBusiness(Long businessId, int page, int size) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Contract> contracts = contractRepository.findByBusinessId(businessId, pageable);

        return contracts.map(ContractDTO::new);
    }

    @Transactional
    public ContractDTO createContract(Long businessId, ContractRequest request, Long currentUserId) throws IOException {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        // if (!business.getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to create contracts
        // for this business");
        // }

        B2BPartner partner = b2bPartnerService.getPartnerById(request.getPartnerId());

        // Verify partner belongs to this business
        if (!partner.getBusiness().getId().equals(businessId)) {
            throw new IllegalArgumentException("Partner does not belong to this business");
        }

        // Verify partner is active
        // if (partner.getStatus() != B2BPartnerStatus.ACTIVE) {
        // throw new IllegalStateException("Cannot create contract with non-active
        // partner");
        // }

        // Parse dates
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDate startDate = LocalDate.parse(request.getStartDate().substring(0, 10), DateTimeFormatter.ISO_DATE);
        LocalDate endDate = LocalDate.parse(request.getEndDate().substring(0, 10), DateTimeFormatter.ISO_DATE);

        // Validate dates
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Contract contract = new Contract();
        contract.setBusiness(business);
        contract.setPartner(partner);
        contract.setTitle(request.getTitle());
        contract.setDescription(request.getDescription());
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setTerms(request.getTerms());
        contract.setStatus(ContractStatus.DRAFT);

        // Process document uploads
        List<String> documentUrls = new ArrayList<>();
        List<MultipartFile> documents = request.getDocuments();
        if (documents != null && !documents.isEmpty()) {
            for (MultipartFile document : documents) {
                // Validate file size and type
                validateDocument(document);

                // Save file
                String fileName = UUID.randomUUID().toString() + "_" + document.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.write(filePath, document.getBytes());

                documentUrls.add(fileName);
            }
        }

        contract.setDocuments(documentUrls);

        Contract savedContract = contractRepository.save(contract);
        return new ContractDTO(savedContract);
    }

    private void validateDocument(MultipartFile document) {
        // Check file size (max 10MB)
        if (document.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit (10MB)");
        }

        // Check file type
        String contentType = document.getContentType();
        if (contentType == null || !(contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new IllegalArgumentException("Only PDF and Word documents are allowed");
        }
    }

    @Transactional
    public ContractDTO updateContract(Long contractId, ContractRequest request, Long currentUserId) throws IOException {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        // if (!contract.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to update this
        // contract");
        // }

        // Parse dates
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        LocalDate startDate = LocalDate.parse(request.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(request.getEndDate(), formatter);

        // Validate dates
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Update contract fields
        contract.setTitle(request.getTitle());
        contract.setDescription(request.getDescription());
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setTerms(request.getTerms());

        // Process new document uploads
        List<String> documentUrls = new ArrayList<>(contract.getDocuments());
        List<MultipartFile> newDocuments = request.getDocuments();
        if (newDocuments != null && !newDocuments.isEmpty()) {
            for (MultipartFile document : newDocuments) {
                // Validate file
                validateDocument(document);

                // Save file
                String fileName = UUID.randomUUID().toString() + "_" + document.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.write(filePath, document.getBytes());

                documentUrls.add(fileName);
            }
        }

        contract.setDocuments(documentUrls);

        Contract updatedContract = contractRepository.save(contract);
        return new ContractDTO(updatedContract);
    }

    @Transactional
    public void deleteContract(Long contractId, Long currentUserId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        // if (!contract.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to delete this
        // contract");
        // }

        contractRepository.delete(contract);
    }

    @Transactional(readOnly = true)
    public ContractDTO getContractById(Long contractId, Long currentUserId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        // Check if user is authorized to view this contract
        // if (!contract.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("You are not authorized to view this
        // contract");
        // }

        return new ContractDTO(contract);
    }

    @Transactional
    public ContractDTO createContract(Long businessId, ContractDTO contractDTO, List<MultipartFile> documents,
            Long currentUserId) throws IOException {
        // Create a ContractRequest from the DTO and documents
        ContractRequest request = new ContractRequest();
        request.setPartnerId(contractDTO.getPartnerId());
        request.setTitle(contractDTO.getTitle());
        request.setDescription(contractDTO.getDescription());
        request.setStartDate(contractDTO.getStartDate());
        request.setEndDate(contractDTO.getEndDate());
        request.setTerms(contractDTO.getTerms());
        request.setDocuments(documents);

        // Delegate to the new method
        return createContract(businessId, request, currentUserId);
    }

    @Transactional
    public ContractDTO updateContract(Long contractId, ContractDTO contractDTO, List<MultipartFile> newDocuments,
            Long currentUserId) throws IOException {
        // Create a ContractRequest from the DTO and documents
        ContractRequest request = new ContractRequest();
        request.setPartnerId(contractDTO.getPartnerId());
        request.setTitle(contractDTO.getTitle());
        request.setDescription(contractDTO.getDescription());
        request.setStartDate(contractDTO.getStartDate());
        request.setEndDate(contractDTO.getEndDate());
        request.setTerms(contractDTO.getTerms());
        request.setDocuments(newDocuments);

        // Delegate to the new method
        return updateContract(contractId, request, currentUserId);
    }
}