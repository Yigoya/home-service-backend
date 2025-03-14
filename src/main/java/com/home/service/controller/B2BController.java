package com.home.service.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.B2BPartnerService;
import com.home.service.Service.B2BPartnerService.B2BPartnerDTO;
import com.home.service.Service.B2BPartnerService.PartnerRequestDTO;
import com.home.service.Service.B2BPartnerService.BusinessPartnersResponse;
import com.home.service.Service.ContractService;
import com.home.service.Service.ContractService.ContractDTO;
import com.home.service.Service.QuoteService;
import com.home.service.Service.QuoteService.QuoteDTO;
import com.home.service.models.enums.B2BPartnerStatus;
import com.home.service.models.enums.ContractStatus;
import com.home.service.models.enums.QuoteStatus;

@RestController
public class B2BController {

    private final B2BPartnerService b2bPartnerService;
    private final ContractService contractService;
    private final QuoteService quoteService;

    public B2BController(B2BPartnerService b2bPartnerService, ContractService contractService,
            QuoteService quoteService) {
        this.b2bPartnerService = b2bPartnerService;
        this.contractService = contractService;
        this.quoteService = quoteService;
    }

    // Contract Request Class for handling multipart form data
    public static class ContractRequest {
        private Long partnerId;
        private String title;
        private String description;
        private String startDate;
        private String endDate;
        private String terms;
        private List<MultipartFile> documents;
        private ContractStatus status;

        public Long getPartnerId() {
            return partnerId;
        }

        public void setPartnerId(Long partnerId) {
            this.partnerId = partnerId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getTerms() {
            return terms;
        }

        public void setTerms(String terms) {
            this.terms = terms;
        }

        public List<MultipartFile> getDocuments() {
            return documents;
        }

        public void setDocuments(List<MultipartFile> documents) {
            this.documents = documents;
        }
    }

    // B2B Partner Endpoints

    @GetMapping("/businesses/{businessId}/partners")
    public ResponseEntity<BusinessPartnersResponse> getPartnersByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        BusinessPartnersResponse response = b2bPartnerService.getAllBusinessPartners(businessId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/businesses/{businessId}/partners/created")
    public ResponseEntity<Page<B2BPartnerDTO>> getPartnersCreatedByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        Page<B2BPartnerDTO> partners = b2bPartnerService.getPartnersByBusiness(businessId, page, size);
        return ResponseEntity.ok(partners);
    }

    @GetMapping("/businesses/{businessId}/partners/requests")
    public ResponseEntity<Page<PartnerRequestDTO>> getPartnerRequestsForBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        Page<PartnerRequestDTO> requests = b2bPartnerService.getPartnerRequestsByBusiness(businessId, page, size);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/businesses/{businessId}/partners")
    public ResponseEntity<B2BPartnerDTO> createPartner(
            @PathVariable Long businessId,
            @RequestBody B2BPartnerDTO partnerDTO) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        B2BPartnerDTO partner = b2bPartnerService.createPartner(businessId, partnerDTO, currentUserId);
        return new ResponseEntity<>(partner, HttpStatus.CREATED);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/businesses/partners/{partnerId}")
    public ResponseEntity<B2BPartnerDTO> updatePartner(
            @PathVariable Long partnerId,
            @RequestBody B2BPartnerDTO partnerDTO) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        B2BPartnerDTO partner = b2bPartnerService.updatePartner(partnerId, partnerDTO, currentUserId);
        return ResponseEntity.ok(partner);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/businesses/partners/{partnerId}/status")
    public ResponseEntity<B2BPartnerDTO> updatePartnerStatus(
            @PathVariable Long partnerId,
            @RequestParam B2BPartnerStatus status) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        B2BPartnerDTO partner = b2bPartnerService.updatePartnerStatus(partnerId, status, currentUserId);
        return ResponseEntity.ok(partner);
    }

    @DeleteMapping("/businesses/partners/{partnerId}")
    public ResponseEntity<Void> deletePartner(@PathVariable Long partnerId) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        b2bPartnerService.deletePartner(partnerId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    // Contract Endpoints
    @GetMapping("/businesses/{businessId}/contracts")
    public ResponseEntity<Page<ContractDTO>> getContractsByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ContractDTO> contracts = contractService.getContractsByBusiness(businessId, page, size);
        return ResponseEntity.ok(contracts);
    }

    @PostMapping(value = "/businesses/{businessId}/contracts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractDTO> createContract(
            @PathVariable Long businessId,
            @ModelAttribute ContractRequest request) throws IOException {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        ContractDTO contract = contractService.createContract(businessId, request, currentUserId);
        return new ResponseEntity<>(contract, HttpStatus.CREATED);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping(value = "/businesses/contracts/{contractId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractDTO> updateContract(
            @PathVariable Long contractId,
            @ModelAttribute ContractRequest request) throws IOException {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        ContractDTO contract = contractService.updateContract(contractId, request, currentUserId);
        return ResponseEntity.ok(contract);
    }

    @DeleteMapping("/businesses/contracts/{contractId}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long contractId) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        contractService.deleteContract(contractId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    // Quote Endpoints
    @GetMapping("/businesses/{businessId}/quotes")
    public ResponseEntity<Page<QuoteDTO>> getQuotesByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<QuoteDTO> quotes = quoteService.getQuotesByBusiness(businessId, page, size);
        return ResponseEntity.ok(quotes);
    }

    @PostMapping("/businesses/{businessId}/quotes")
    public ResponseEntity<QuoteDTO> createQuote(
            @PathVariable Long businessId,
            @RequestBody QuoteDTO quoteDTO) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        QuoteDTO quote = quoteService.createQuote(businessId, quoteDTO, currentUserId);
        return new ResponseEntity<>(quote, HttpStatus.CREATED);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/businesses/quotes/{quoteId}")
    public ResponseEntity<QuoteDTO> updateQuote(
            @PathVariable Long quoteId,
            @RequestBody QuoteDTO quoteDTO) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        QuoteDTO quote = quoteService.updateQuote(quoteId, quoteDTO, currentUserId);
        return ResponseEntity.ok(quote);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/businesses/quotes/{quoteId}/status")
    public ResponseEntity<QuoteDTO> updateQuoteStatus(
            @PathVariable Long quoteId,
            @RequestParam QuoteStatus status) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        QuoteDTO quote = quoteService.updateQuoteStatus(quoteId, status, currentUserId);
        return ResponseEntity.ok(quote);
    }

    @CrossOrigin(originPatterns = "*")
    @DeleteMapping("/businesses/quotes/{quoteId}")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long quoteId) {
        // Long currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 4L; // Replace with actual user ID retrieval logic
        quoteService.deleteQuote(quoteId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}