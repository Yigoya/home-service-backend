package com.home.service.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.models.Technician;
import com.home.service.models.TechnicianDocument;
import com.home.service.models.TechnicianProofResponse;
import com.home.service.models.User;
import com.home.service.models.enums.AccountStatus;
import com.home.service.models.enums.DocumentStatus;
import com.home.service.repositories.TechnicianDocumentRepository;
import com.home.service.repositories.TechnicianRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.EmailService;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class PaymentProofService {

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private TechnicianDocumentRepository technicianDocumentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public String uploadPaymentProof(MultipartFile file, Long technicianId) {
        // Find the technician
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

        // Save the uploaded file (assume a method saveFile returns the file URL)
        String fileUrl = fileStorageService.storeFile(file);

        // Create and save the document entry
        TechnicianDocument document = new TechnicianDocument();
        document.setTechnician(technician);
        document.setDocumentType("BANK_TICKET");
        document.setFileName(file.getOriginalFilename());
        document.setFileUrl(fileUrl);
        document.setStatus(DocumentStatus.PENDING);

        technicianDocumentRepository.save(document);

        return "Payment proof uploaded successfully and pending review.";
    }

    public String reviewPaymentProof(Long technicianId, boolean approve) {
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

        // Retrieve pending documents for the technician
        List<TechnicianDocument> pendingDocuments = technicianDocumentRepository.findByTechnicianAndStatus(technician,
                DocumentStatus.PENDING);

        if (pendingDocuments.isEmpty()) {
            return "No pending documents found for review.";
        }

        // Update document status and technician's account status based on the admin's
        // decision
        for (TechnicianDocument document : pendingDocuments) {
            document.setStatus(approve ? DocumentStatus.APPROVED : DocumentStatus.REJECTED);
            technicianDocumentRepository.save(document);
        }

        if (approve) {
            technician.setVerified(true);
            technicianRepository.save(technician);
            User user = technician.getUser();
            user.setStatus(AccountStatus.ACTIVE);
            userRepository.save(user);
            emailService.sendApprovalEmail(technician.getUser());
            return "Technician approved and account activated.";
        } else {
            emailService.sendRejectionEmail(technician.getUser());
            return "Technician's document declined.";
        }
    }

    @Transactional
    public List<TechnicianProofResponse> getTechniciansWithPendingProofs() {
        // Fetch pending technician documents
        List<TechnicianDocument> pendingDocuments = technicianDocumentRepository.findByStatus(DocumentStatus.PENDING);

        // Map to response DTOs
        return pendingDocuments.stream()
                .map(doc -> new TechnicianProofResponse(
                        doc.getTechnician().getId(),
                        doc.getTechnician().getUser().getName(),
                        doc.getTechnician().getUser().getEmail(),
                        doc.getFileUrl(),
                        doc.getDocumentType(),
                        doc.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
