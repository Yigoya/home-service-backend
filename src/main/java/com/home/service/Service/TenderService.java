package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.dto.ServiceRequest;
import com.home.service.dto.SubscriptionRequest;
import com.home.service.dto.TenderCreationRequest;
import com.home.service.dto.TenderDTO;
import com.home.service.dto.TenderRequest;
import com.home.service.dto.TenderResponse;
import com.home.service.dto.TenderStatusRequest;
import com.home.service.dto.TenderUpdateRequest;
import com.home.service.models.Customer;
import com.home.service.models.CustomerSubscription;
import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.Services;
import com.home.service.models.Tender;
import com.home.service.models.TenderAgencyProfile;
import com.home.service.models.TenderSubscriptionPlan;
import com.home.service.models.enums.SubscriptionStatus;
import com.home.service.models.enums.TenderStatus;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.CustomerSubscriptionRepository;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.TenderAgencyProfileRepository;
import com.home.service.repositories.TenderRepository;
import com.home.service.repositories.TenderSubscriptionPlanRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@Service
public class TenderService {

    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private ServiceRepository servicesRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenderAgencyProfileRepository agencyRepository;

    @Autowired
    private TenderSubscriptionPlanRepository planRepository;

    @Autowired
    private CustomerSubscriptionRepository subscriptionRepository;

    public String addTender(TenderRequest tenderDTO) throws IOException {
        Services category = servicesRepository.findById(tenderDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Tender tender = new Tender();
        tender.setTitle(tenderDTO.getTitle());
        tender.setDescription(tenderDTO.getDescription());
        tender.setLocation(tenderDTO.getLocation());
        tender.setClosingDate(tenderDTO.getClosingDate());
        tender.setContactInfo(tenderDTO.getContactInfo());
        tender.setService(category);
        tender.setStatus(tenderDTO.getStatus() != null ? tenderDTO.getStatus() : TenderStatus.OPEN);
        tender.setDatePosted(LocalDateTime.now());
        // default to false if not specified
        tender.setFree(Boolean.TRUE.equals(tenderDTO.getIsFree()));
        tender.setReferenceNumber(tenderDTO.getReferenceNumber());
        tender.setNoticeNumber(tenderDTO.getNoticeNumber());
        tender.setProductCategory(tenderDTO.getProductCategory());
        tender.setTenderType(tenderDTO.getTenderType());
        tender.setProcurementMethod(tenderDTO.getProcurementMethod());
        tender.setCostOfTenderDocument(tenderDTO.getCostOfTenderDocument());
        tender.setBidValidity(tenderDTO.getBidValidity());
        tender.setBidSecurity(tenderDTO.getBidSecurity());
        tender.setContractPeriod(tenderDTO.getContractPeriod());
        tender.setPerformanceSecurity(tenderDTO.getPerformanceSecurity());
        tender.setPaymentTerms(tenderDTO.getPaymentTerms());
        tender.setKeyDeliverables(tenderDTO.getKeyDeliverables());
        tender.setTechnicalSpecifications(tenderDTO.getTechnicalSpecifications());
        tender.setTenderReferenceNoticeNo(tenderDTO.getTenderReferenceNoticeNo());
        tender.setPublishedOn(tenderDTO.getPublishedOn());
        tender.setBidSubmissionDeadline(tenderDTO.getBidSubmissionDeadline());
        tender.setTenderNoticeCode(tenderDTO.getTenderNoticeCode());
        tender.setWarranty(tenderDTO.getWarranty());
        tender.setGeneralEligibility(tenderDTO.getGeneralEligibility());
        tender.setTechnicalRequirements(tenderDTO.getTechnicalRequirements());
        tender.setFinancialRequirements(tenderDTO.getFinancialRequirements());
        tender.setExperience(tenderDTO.getExperience());
        tender.setPreBidMeeting(tenderDTO.getPreBidMeeting());
        tender.setSiteVisit(tenderDTO.getSiteVisit());
        tender.setDeadlineForClarifications(tenderDTO.getDeadlineForClarifications());
        tender.setBidOpeningDate(tenderDTO.getBidOpeningDate());
        tender.setTenderDocumentCollectionLocation(tenderDTO.getTenderDocumentCollectionLocation());
        tender.setTenderDocumentCollectionTime(tenderDTO.getTenderDocumentCollectionTime());
        tender.setTenderDocumentDownload(tenderDTO.getTenderDocumentDownload());
        tender.setBidSubmissionMode(tenderDTO.getBidSubmissionMode());
        tender.setBidSubmissionAddress(tenderDTO.getBidSubmissionAddress());
        tender.setOrganization(tenderDTO.getOrganization());
        tender.setDepartment(tenderDTO.getDepartment());
        tender.setAddress(tenderDTO.getAddress());
        tender.setTenderLocation(tenderDTO.getTenderLocation());
        tender.setLanguageOfBids(tenderDTO.getLanguageOfBids());
        tender.setValidityPeriodOfBids(tenderDTO.getValidityPeriodOfBids());
        tender.setGoverningLaw(tenderDTO.getGoverningLaw());

        if (tenderDTO.getFile() != null) {
            String filePath = fileStorageService.storeFile(tenderDTO.getFile());
            tender.setDocumentPath(filePath);
        }

        tenderRepository.save(tender);
        return "Tender added successfully";
    }

    @Transactional
    public TenderResponse addAgencyTender(TenderCreationRequest request, Long agencyId) {
        TenderAgencyProfile agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));

        Services service = servicesRepository.findById(request.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        Tender tender = new Tender();
        tender.setTitle(request.getTitle());
        tender.setDescription(request.getDescription());
        tender.setLocation(request.getLocation());
        tender.setClosingDate(request.getClosingDate());
        tender.setContactInfo(request.getContactInfo());
        tender.setService(service);
        tender.setAgency(agency);
        tender.setQuestionDeadline(request.getQuestionDeadline());
        tender.setStatus(TenderStatus.OPEN);
        tender.setFree(Boolean.TRUE.equals(request.getIsFree()));
        tender.setReferenceNumber(request.getReferenceNumber());
        tender.setNoticeNumber(request.getNoticeNumber());
        tender.setProductCategory(request.getProductCategory());
        tender.setTenderType(request.getTenderType());
        tender.setProcurementMethod(request.getProcurementMethod());
        tender.setCostOfTenderDocument(request.getCostOfTenderDocument());
        tender.setBidValidity(request.getBidValidity());
        tender.setBidSecurity(request.getBidSecurity());
        tender.setContractPeriod(request.getContractPeriod());
        tender.setPerformanceSecurity(request.getPerformanceSecurity());
        tender.setPaymentTerms(request.getPaymentTerms());
        tender.setKeyDeliverables(request.getKeyDeliverables());
        tender.setTechnicalSpecifications(request.getTechnicalSpecifications());
        tender.setTenderReferenceNoticeNo(request.getTenderReferenceNoticeNo());
        tender.setPublishedOn(request.getPublishedOn());
        tender.setBidSubmissionDeadline(request.getBidSubmissionDeadline());
        tender.setTenderNoticeCode(request.getTenderNoticeCode());
        tender.setWarranty(request.getWarranty());
        tender.setGeneralEligibility(request.getGeneralEligibility());
        tender.setTechnicalRequirements(request.getTechnicalRequirements());
        tender.setFinancialRequirements(request.getFinancialRequirements());
        tender.setExperience(request.getExperience());
        tender.setPreBidMeeting(request.getPreBidMeeting());
        tender.setSiteVisit(request.getSiteVisit());
        tender.setDeadlineForClarifications(request.getDeadlineForClarifications());
        tender.setBidOpeningDate(request.getBidOpeningDate());
        tender.setTenderDocumentCollectionLocation(request.getTenderDocumentCollectionLocation());
        tender.setTenderDocumentCollectionTime(request.getTenderDocumentCollectionTime());
        tender.setTenderDocumentDownload(request.getTenderDocumentDownload());
        tender.setBidSubmissionMode(request.getBidSubmissionMode());
        tender.setBidSubmissionAddress(request.getBidSubmissionAddress());
        tender.setOrganization(request.getOrganization());
        tender.setDepartment(request.getDepartment());
        tender.setAddress(request.getAddress());
        tender.setTenderLocation(request.getTenderLocation());
        tender.setLanguageOfBids(request.getLanguageOfBids());
        tender.setValidityPeriodOfBids(request.getValidityPeriodOfBids());
        tender.setGoverningLaw(request.getGoverningLaw());

        Tender savedTender = tenderRepository.save(tender);
        return mapToTenderResponse(savedTender);
    }

    @Transactional
    public CustomerSubscription createSubscription(SubscriptionRequest request, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        TenderSubscriptionPlan plan = planRepository.findByPlanId(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        CustomerSubscription subscription = new CustomerSubscription();
        subscription.setCustomer(customer);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDateTime.now());

        Duration duration = getDurationFromPlan(plan.getDuration());
        subscription.setEndDate(LocalDateTime.now().plus(duration));

        subscription.setWhatsappNumber(request.getWhatsappNumber());
        subscription.setTelegramUsername(request.getTelegramUsername());
        subscription.setFollowedServiceIds(request.getServiceIds());
        subscription.setCompanyName(request.getCompanyName());
        subscription.setTinNumber(request.getTinNumber());
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        return subscriptionRepository.save(subscription);
    }

    private Duration getDurationFromPlan(String duration) {
        switch (duration) {
            case "1 Month":
                return Duration.ofDays(30);
            case "3 Months":
                return Duration.ofDays(90);
            case "6 Months":
                return Duration.ofDays(180);
            case "12 Months":
                return Duration.ofDays(365);
            default:
                return Duration.ZERO;
        }
    }

    private void collectServiceIdsRecursively(Services service, List<Long> ids) {
        ids.add(service.getId());
        for (Services childService : service.getServices()) {
            collectServiceIdsRecursively(childService, ids);
        }
    }

    public String updateTender(TenderRequest tenderDTO, MultipartFile file) throws IOException {
        Tender tender = tenderRepository.findById(tenderDTO.getId())
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        tender.setTitle(tenderDTO.getTitle());
        tender.setDescription(tenderDTO.getDescription());
        tender.setLocation(tenderDTO.getLocation());
        tender.setClosingDate(tenderDTO.getClosingDate());
        tender.setContactInfo(tenderDTO.getContactInfo());
        tender.setStatus(tenderDTO.getStatus());
        if (tenderDTO.getIsFree() != null) {
            tender.setFree(Boolean.TRUE.equals(tenderDTO.getIsFree()));
        }
        tender.setReferenceNumber(tenderDTO.getReferenceNumber());
        tender.setNoticeNumber(tenderDTO.getNoticeNumber());
        tender.setProductCategory(tenderDTO.getProductCategory());
        tender.setTenderType(tenderDTO.getTenderType());
        tender.setProcurementMethod(tenderDTO.getProcurementMethod());
        tender.setCostOfTenderDocument(tenderDTO.getCostOfTenderDocument());
        tender.setBidValidity(tenderDTO.getBidValidity());
        tender.setBidSecurity(tenderDTO.getBidSecurity());
        tender.setContractPeriod(tenderDTO.getContractPeriod());
        tender.setPerformanceSecurity(tenderDTO.getPerformanceSecurity());
        tender.setPaymentTerms(tenderDTO.getPaymentTerms());
        tender.setKeyDeliverables(tenderDTO.getKeyDeliverables());
        tender.setTechnicalSpecifications(tenderDTO.getTechnicalSpecifications());
        tender.setTenderReferenceNoticeNo(tenderDTO.getTenderReferenceNoticeNo());
        tender.setPublishedOn(tenderDTO.getPublishedOn());
        tender.setBidSubmissionDeadline(tenderDTO.getBidSubmissionDeadline());
        tender.setTenderNoticeCode(tenderDTO.getTenderNoticeCode());
        tender.setWarranty(tenderDTO.getWarranty());
        tender.setGeneralEligibility(tenderDTO.getGeneralEligibility());
        tender.setTechnicalRequirements(tenderDTO.getTechnicalRequirements());
        tender.setFinancialRequirements(tenderDTO.getFinancialRequirements());
        tender.setExperience(tenderDTO.getExperience());
        tender.setPreBidMeeting(tenderDTO.getPreBidMeeting());
        tender.setSiteVisit(tenderDTO.getSiteVisit());
        tender.setDeadlineForClarifications(tenderDTO.getDeadlineForClarifications());
        tender.setBidOpeningDate(tenderDTO.getBidOpeningDate());
        tender.setTenderDocumentCollectionLocation(tenderDTO.getTenderDocumentCollectionLocation());
        tender.setTenderDocumentCollectionTime(tenderDTO.getTenderDocumentCollectionTime());
        tender.setTenderDocumentDownload(tenderDTO.getTenderDocumentDownload());
        tender.setBidSubmissionMode(tenderDTO.getBidSubmissionMode());
        tender.setBidSubmissionAddress(tenderDTO.getBidSubmissionAddress());
        tender.setOrganization(tenderDTO.getOrganization());
        tender.setDepartment(tenderDTO.getDepartment());
        tender.setAddress(tenderDTO.getAddress());
        tender.setTenderLocation(tenderDTO.getTenderLocation());
        tender.setLanguageOfBids(tenderDTO.getLanguageOfBids());
        tender.setValidityPeriodOfBids(tenderDTO.getValidityPeriodOfBids());
        tender.setGoverningLaw(tenderDTO.getGoverningLaw());

        if (tenderDTO.getCategoryId() != null) {
            Services category = servicesRepository.findById(tenderDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            tender.setService(category);
        }

        if (file != null && !file.isEmpty()) {
            String filePath = fileStorageService.storeFile(file);
            tender.setDocumentPath(filePath);
        }

        tenderRepository.save(tender);
        return "Tender updated successfully";
    }

    @Transactional
    public String addService(ServiceRequest serviceRequest) {

        ServiceCategory category = serviceCategoryRepository.findById(3L)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        Services service = new Services();
        service.setCategory(category);
        service.setEstimatedDuration(serviceRequest.getEstimatedDuration());
        service.setServiceFee(serviceRequest.getServiceFee());

        if (serviceRequest.getIcon() != null) {
            String icon = fileStorageService.storeFile(serviceRequest.getIcon());
            service.setIcon(icon);
        }

        // Create the translation and link it to the service
        ServiceTranslation translation = new ServiceTranslation();
        translation.setLang(serviceRequest.getLang());
        translation.setName(serviceRequest.getName());
        translation.setDescription(serviceRequest.getDescription());
        translation.setService(service);
        service.getTranslations().add(translation);

        if (serviceRequest.getServiceId() != null) {
            Services parentService = serviceRepository.findById(serviceRequest.getServiceId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found"));
            parentService.getServices().add(service);
            serviceRepository.save(parentService);
        } else {
            serviceRepository.save(service);
        }

        return "Service added successfully with ID: " + service.getId();
    }

    public TenderDTO changeTenderStatus(Long id, TenderStatus status) {
        return tenderRepository.findById(id).map(tender -> {
            tender.setStatus(status);
            return TenderDTO.createWithFullDetails(tenderRepository.save(tender));
        }).orElseThrow(() -> new RuntimeException("Tender not found"));
    }

    public void deleteTender(Long id) {
        tenderRepository.deleteById(id);
    }

    public Page<TenderDTO> getTendersByService(Long servicesId, int page, int size) {
        List<Long> ids = new ArrayList<>();
        Services service = servicesRepository.findById(servicesId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        collectServiceIdsRecursively(service, ids);
        Pageable pageable = PageRequest.of(page, size);
        return tenderRepository.findByServiceIdIn(ids, pageable).map(TenderDTO::createWithoutSensitiveDetails);
    }

    public Page<TenderDTO> getTendersByServices(List<Long> servicesIds, int page, int size) {
        List<Long> allIds = new ArrayList<>();
        for (Long id : servicesIds) {
            Services service = servicesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Service not found"));
            collectServiceIdsRecursively(service, allIds);
        }
        Pageable pageable = PageRequest.of(page, size);
        return tenderRepository.findByServiceIdIn(allIds, pageable).map(TenderDTO::createWithoutSensitiveDetails);
    }

    public Page<TenderDTO> getTendersByStatus(TenderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tenderRepository.findByStatus(status, pageable).map(TenderDTO::createWithoutSensitiveDetails);
    }

    public Page<TenderDTO> getAllTenders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tenderRepository.findAll(pageable).map(TenderDTO::createWithoutSensitiveDetails);
    }

    public TenderDTO getTenderById(Long id) {
        return tenderRepository.findById(id)
                .map(TenderDTO::createWithFullDetails)
                .orElseThrow(() -> new RuntimeException("Tender not found"));
    }

    public Page<TenderDTO> getTendersByLocationAndService(String location, Long serviceId, int page, int size) {
        List<Long> ids = new ArrayList<>();
        Services service = servicesRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        collectServiceIdsRecursively(service, ids);
        Pageable pageable = PageRequest.of(page, size);
        System.out.println(ids);
        return tenderRepository.findByLocationAndServiceIdIn(location, ids, pageable)
                .map(TenderDTO::createWithoutSensitiveDetails);
    }

    public Page<TenderDTO> getTendersByLocation(String location, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tenderRepository.findByLocation(location, pageable).map(TenderDTO::createWithoutSensitiveDetails);
    }

    public Page<TenderDTO> searchTenders(String keyword, TenderStatus status, String location, List<Long> serviceIds,
            Boolean isFree, LocalDateTime datePosted, LocalDateTime closingDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Long> expandedServiceIds = new ArrayList<>();
        if (serviceIds != null) {
            for (Long sid : serviceIds) {
                Services service = servicesRepository.findById(sid)
                        .orElseThrow(() -> new RuntimeException("Service not found"));
                collectServiceIdsRecursively(service, expandedServiceIds);
            }
        }
        if (expandedServiceIds.isEmpty()) {
            expandedServiceIds = null; // Handle empty list case
        }
        System.out.println(
                keyword + " " + status + " " + location + " " + serviceIds + " " + datePosted + " " + closingDate);
        return tenderRepository.advancedSearch(keyword, status, location, expandedServiceIds, isFree, datePosted, closingDate, pageable)
                .map(TenderDTO::createWithoutSensitiveDetails);
    }

    public Page<Tender> advancedSearch(
            String keyword,
            List<Long> categoryIds,
            String location,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            TenderStatus status,
        Boolean isFree,
            Pageable pageable) {
        Specification<Tender> spec = Specification.where(null);

        if (keyword != null) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.get("title"), "%" + keyword + "%"),
                    cb.like(root.get("description"), "%" + keyword + "%")));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("service").get("id").in(categoryIds));
        }

        if (location != null) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("location"), "%" + location + "%"));
        }

        if (dateFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("datePosted"), dateFrom));
        }

        if (dateTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("datePosted"), dateTo));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (isFree != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("free"), isFree));
        }

        return tenderRepository.findAll(spec, pageable);
    }

    public List<Tender> getArchiveTenders(Long customerId) {
        // Assuming archived tenders are those with CLOSED status older than 30 days
        return tenderRepository.findByStatusAndDatePostedBefore(
                TenderStatus.CLOSED,
                LocalDateTime.now().minusDays(30));
    }

    public List<TenderResponse> getAgencyTenders(Long agencyId, int page, int size, String sort) {
        TenderAgencyProfile agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));

        // Robust sort parsing with safe defaults
        Sort.Direction direction = Sort.Direction.DESC;
        String sortField = "datePosted";
        if (sort != null && !sort.isBlank()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length >= 1 && !sortParams[0].isBlank()) {
                sortField = sortParams[0].trim();
            }
            if (sortParams.length >= 2 && !sortParams[1].isBlank()) {
                try {
                    direction = Sort.Direction.fromString(sortParams[1].trim());
                } catch (IllegalArgumentException ex) {
                    // accept common shorthand like 'des'
                    String dir = sortParams[1].trim().toLowerCase();
                    if (dir.startsWith("des")) direction = Sort.Direction.DESC;
                    else if (dir.startsWith("asc")) direction = Sort.Direction.ASC;
                }
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return tenderRepository.findByAgency(agency, pageable)
                .stream()
                .map(this::mapToTenderResponse)
                .collect(Collectors.toList());
    }

    public TenderResponse getTender(Long agencyId, Long tenderId) {
        Tender tender = tenderRepository.findByIdAndAgencyId(tenderId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Tender not found"));
        return mapToTenderResponse(tender);
    }

    public TenderResponse updateTender(Long agencyId, Long tenderId, TenderUpdateRequest request) {
        Tender tender = tenderRepository.findByIdAndAgencyId(tenderId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Tender not found"));

        tender.setTitle(request.getTitle());
        tender.setDescription(request.getDescription());
        tender.setLocation(request.getLocation());
        tender.setClosingDate(request.getClosingDate());
        tender.setContactInfo(request.getContactInfo());
        tender.setQuestionDeadline(request.getQuestionDeadline());
        tender.setReferenceNumber(request.getReferenceNumber());
        tender.setNoticeNumber(request.getNoticeNumber());
        tender.setProductCategory(request.getProductCategory());
        tender.setTenderType(request.getTenderType());
        tender.setProcurementMethod(request.getProcurementMethod());
        tender.setCostOfTenderDocument(request.getCostOfTenderDocument());
        tender.setBidValidity(request.getBidValidity());
        tender.setBidSecurity(request.getBidSecurity());
        tender.setContractPeriod(request.getContractPeriod());
        tender.setPerformanceSecurity(request.getPerformanceSecurity());
        tender.setPaymentTerms(request.getPaymentTerms());
        tender.setKeyDeliverables(request.getKeyDeliverables());
        tender.setTechnicalSpecifications(request.getTechnicalSpecifications());
        if (request.getIsFree() != null) {
            tender.setFree(Boolean.TRUE.equals(request.getIsFree()));
        }

        Tender updatedTender = tenderRepository.save(tender);
        return mapToTenderResponse(updatedTender);
    }

    public String uploadTenderDocument(Long agencyId, Long tenderId, MultipartFile file) {
        Tender tender = tenderRepository.findByIdAndAgencyId(tenderId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Tender not found"));

        String filePath = fileStorageService.storeFile(file);
        tender.setDocumentPath(filePath);
        tenderRepository.save(tender);
        return filePath;
    }

    public TenderResponse updateTenderStatus(Long agencyId, Long tenderId, TenderStatusRequest request) {
        Tender tender = tenderRepository.findByIdAndAgencyId(tenderId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Tender not found"));

        tender.setStatus(request.getStatus());
        Tender updatedTender = tenderRepository.save(tender);
        return mapToTenderResponse(updatedTender);
    }

    public void deleteTender(Long agencyId, Long tenderId) {
        Tender tender = tenderRepository.findByIdAndAgencyId(tenderId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Tender not found"));
        tenderRepository.delete(tender);
    }

    private TenderResponse mapToTenderResponse(Tender tender) {
        TenderResponse response = new TenderResponse();
        response.setId(tender.getId());
        response.setTitle(tender.getTitle());
        response.setDescription(tender.getDescription());
        response.setLocation(tender.getLocation());
        response.setDatePosted(tender.getDatePosted());
        response.setClosingDate(tender.getClosingDate());
        response.setContactInfo(tender.getContactInfo());
        response.setReferenceNumber(tender.getReferenceNumber());
        response.setNoticeNumber(tender.getNoticeNumber());
        response.setProductCategory(tender.getProductCategory());
        response.setTenderType(tender.getTenderType());
        response.setProcurementMethod(tender.getProcurementMethod());
        response.setCostOfTenderDocument(tender.getCostOfTenderDocument());
        response.setBidValidity(tender.getBidValidity());
        response.setBidSecurity(tender.getBidSecurity());
        response.setContractPeriod(tender.getContractPeriod());
        response.setPerformanceSecurity(tender.getPerformanceSecurity());
        response.setPaymentTerms(tender.getPaymentTerms());
        response.setKeyDeliverables(tender.getKeyDeliverables());
        response.setTechnicalSpecifications(tender.getTechnicalSpecifications());
        response.setTenderReferenceNoticeNo(tender.getTenderReferenceNoticeNo());
        response.setPublishedOn(tender.getPublishedOn());
        response.setBidSubmissionDeadline(tender.getBidSubmissionDeadline());
        response.setTenderNoticeCode(tender.getTenderNoticeCode());
        response.setWarranty(tender.getWarranty());
        response.setGeneralEligibility(tender.getGeneralEligibility());
        response.setTechnicalRequirements(tender.getTechnicalRequirements());
        response.setFinancialRequirements(tender.getFinancialRequirements());
        response.setExperience(tender.getExperience());
        response.setPreBidMeeting(tender.getPreBidMeeting());
        response.setSiteVisit(tender.getSiteVisit());
        response.setDeadlineForClarifications(tender.getDeadlineForClarifications());
        response.setBidOpeningDate(tender.getBidOpeningDate());
        response.setTenderDocumentCollectionLocation(tender.getTenderDocumentCollectionLocation());
        response.setTenderDocumentCollectionTime(tender.getTenderDocumentCollectionTime());
        response.setTenderDocumentDownload(tender.getTenderDocumentDownload());
        response.setBidSubmissionMode(tender.getBidSubmissionMode());
        response.setBidSubmissionAddress(tender.getBidSubmissionAddress());
        response.setOrganization(tender.getOrganization());
        response.setDepartment(tender.getDepartment());
        response.setAddress(tender.getAddress());
        response.setTenderLocation(tender.getTenderLocation());
        response.setLanguageOfBids(tender.getLanguageOfBids());
        response.setValidityPeriodOfBids(tender.getValidityPeriodOfBids());
        response.setGoverningLaw(tender.getGoverningLaw());
        response.setStatus(tender.getStatus());
        response.setServiceId(tender.getServiceId());
        response.setDocumentPath(tender.getDocumentPath());
        response.setQuestionDeadline(tender.getQuestionDeadline());
        response.setFree(Boolean.TRUE.equals(tender.getFree()));
        return response;
    }
}
