package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.dto.ServiceRequest;
import com.home.service.dto.SubscriptionRequest;
import com.home.service.dto.TenderDTO;
import com.home.service.dto.TenderRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        if (tenderDTO.getFile() != null) {
            String filePath = fileStorageService.storeFile(tenderDTO.getFile());
            tender.setDocumentPath(filePath);
        }

        tenderRepository.save(tender);
        return "Tender added successfully";
    }

    @Transactional
    public String addAgencyTender(TenderRequest tenderDTO, Long agencyId) throws IOException {
        Services service = servicesRepository.findById(tenderDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Service category not found"));

        TenderAgencyProfile agency = agencyId != null ? agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agency not found"))
                : null;

        Tender tender = new Tender();
        tender.setTitle(tenderDTO.getTitle());
        tender.setDescription(tenderDTO.getDescription());
        tender.setLocation(tenderDTO.getLocation());
        tender.setClosingDate(tenderDTO.getClosingDate());
        tender.setContactInfo(tenderDTO.getContactInfo());
        tender.setService(service);
        tender.setAgency(agency);
        tender.setStatus(tenderDTO.getStatus() != null ? tenderDTO.getStatus() : TenderStatus.OPEN);
        tender.setDatePosted(LocalDateTime.now());
        tender.setQuestionDeadline(LocalDateTime.now().plusWeeks(2));

        if (tenderDTO.getFile() != null && !tenderDTO.getFile().isEmpty()) {
            String filePath = fileStorageService.storeFile(tenderDTO.getFile());
            tender.setDocumentPath(filePath);
        }

        tenderRepository.save(tender);
        return "Tender added successfully";
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

    public Page<TenderDTO> searchTenders(String keyword, TenderStatus status, String location, Long serviceId,
            LocalDateTime datePosted, LocalDateTime closingDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Long> serviceIds = new ArrayList<>();

        if (serviceId != null) {
            Services service = servicesRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Service not found"));
            collectServiceIdsRecursively(service, serviceIds);
        }

        if (serviceIds.isEmpty()) {
            serviceIds = null; // Handle empty list case
        }
        System.out.println(
                keyword + " " + status + " " + location + " " + serviceId + " " + datePosted + " " + closingDate);
        return tenderRepository.advancedSearch(keyword, status, location, serviceIds, datePosted, closingDate, pageable)
                .map(TenderDTO::createWithoutSensitiveDetails);
    }

    public Page<Tender> advancedSearch(
            String keyword,
            String categoryId,
            String location,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            TenderStatus status,
            Pageable pageable) {
        Specification<Tender> spec = Specification.where(null);

        if (keyword != null) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.get("title"), "%" + keyword + "%"),
                    cb.like(root.get("description"), "%" + keyword + "%")));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("service").get("id"), categoryId));
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

        return tenderRepository.findAll(spec, pageable);
    }

    public List<Tender> getArchiveTenders(Long customerId) {
        // Assuming archived tenders are those with CLOSED status older than 30 days
        return tenderRepository.findByStatusAndDatePostedBefore(
                TenderStatus.CLOSED,
                LocalDateTime.now().minusDays(30));
    }
}
