package com.home.service.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.dto.BusinessServiceRequest;
import com.home.service.dto.ServiceDTO;
import com.home.service.models.Business;
import com.home.service.models.BusinessServices;
import com.home.service.models.Services;
import com.home.service.models.BusinessServices.ServiceOption;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.models.enums.ServiceLevel;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.BusinessServiceRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@Service
public class BusinessServiceService {

    private final BusinessServiceRepository businessServiceRepository;
    private final BusinessRepository businessRepository;
    private final ServiceRepository servicesRepository;
    private final FileStorageService fileStorageService;

    public BusinessServiceService(BusinessServiceRepository businessServiceRepository,
            BusinessRepository businessRepository,
            ServiceRepository servicesRepository,
            FileStorageService fileStorageService) {
        this.businessServiceRepository = businessServiceRepository;
        this.businessRepository = businessRepository;
        this.servicesRepository = servicesRepository;
        this.fileStorageService = fileStorageService;
    }

    public static class BusinessServiceDTO {
        public Long id;
        public String name;
        public Long businessId;
        public String description;
        public double price;
        public String image;
        public boolean available;
        public List<ServiceOption> options;

        public BusinessServiceDTO() {
        }

        public BusinessServiceDTO(BusinessServices service) {
            this.id = service.getId();
            this.name = service.getName();
            this.businessId = service.getBusiness().getId();
            this.description = service.getDescription();
            this.price = service.getPrice();
            this.image = service.getImage();
            this.available = service.isAvailable();
            this.options = service.getOptions();
        }
    }

    public BusinessServiceDTO createBusinessService(@Valid BusinessServiceRequest request) {
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Business not found with ID: " + request.getBusinessId()));

        BusinessServices service = new BusinessServices();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setBusiness(business);

        service.setAvailable(request.isAvailable());
        service.setOptions(request.getServiceOptions());

        if (request.getImage() != null) {
            String fileName = fileStorageService.storeFile(request.getImage());
            service.setImage(fileName);
        }

        BusinessServices savedService = businessServiceRepository.save(service);
        return new BusinessServiceDTO(savedService);
    }

    public BusinessServices getBusinessServiceById(Long id) {
        BusinessServices service = businessServiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BusinessService not found with ID: " + id));
        return service;
    }

    public BusinessServiceDTO updateBusinessService(Long id, @Valid BusinessServiceRequest request) {
        BusinessServices service = getBusinessServiceById(id);
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Business not found with ID: " + request.getBusinessId()));
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setBusiness(business);
        service.setAvailable(request.isAvailable());

        // Clear existing options and add new ones
        service.getOptions().clear();
        for (ServiceOption option : request.getServiceOptions()) {
            service.addOption(option);
        }

        if (request.getImage() != null) {
            String fileName = fileStorageService.storeFile(request.getImage());
            service.setImage(fileName);
        }

        BusinessServices updatedService = businessServiceRepository.save(service);
        return new BusinessServiceDTO(updatedService);
    }

    public void deleteBusinessService(Long id) {
        BusinessServices service = getBusinessServiceById(id);
        businessServiceRepository.delete(service);
    }

    public Page<BusinessServiceDTO> getServicesByBusiness(Long businessId, int page, int size) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));
        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessServices> servicesPage = businessServiceRepository.findByBusiness(business, pageable);
        return servicesPage.map(BusinessServiceDTO::new);
    }

    public Page<BusinessServiceDTO> getServices(Long companyId, int page, int size, String sort, String search,
            String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        Sort sorting = Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<BusinessServices> servicesPage = businessServiceRepository.findByCompanyId(companyId, search, pageable);
        return servicesPage.map(BusinessServiceDTO::new);
    }

    public BusinessServiceDTO createService(Long companyId, BusinessServiceDTO dto, String currentUserId) {
        Business company = businessRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        if (!company.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        BusinessServices service = new BusinessServices();
        populateService(service, dto, company);
        BusinessServices savedService = businessServiceRepository.save(service);
        return new BusinessServiceDTO(savedService);
    }

    public BusinessServiceDTO updateService(Long companyId, Long serviceId, BusinessServiceDTO dto,
            String currentUserId) {
        BusinessServices service = businessServiceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
        if (!service.getBusiness().getId().equals(companyId)
                || !service.getBusiness().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        populateService(service, dto, service.getBusiness());
        BusinessServices updatedService = businessServiceRepository.save(service);
        return new BusinessServiceDTO(updatedService);
    }

    public void deleteService(Long companyId, Long serviceId, Long currentUserId) {
        BusinessServices service = businessServiceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
        if (!service.getBusiness().getId().equals(companyId)
                || !service.getBusiness().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        businessServiceRepository.delete(service);
    }

    private void populateService(BusinessServices service, BusinessServiceDTO dto, Business company) {
        service.setName(dto.name);
        service.setDescription(dto.description);
        service.setPrice(dto.price);

        service.setImage(dto.image);
        service.setAvailable(dto.available);
        service.setOptions(dto.options);
        service.setBusiness(company);
    }
}