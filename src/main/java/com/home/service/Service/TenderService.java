package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.dto.ServiceRequest;
import com.home.service.dto.TenderDTO;
import com.home.service.dto.TenderRequest;
import com.home.service.models.AgencyProfile;
import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.Services;
import com.home.service.models.Tender;
import com.home.service.models.enums.TenderStatus;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.TenderRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.nio.file.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale.Category;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    public String addTender(TenderRequest tenderDTO) throws IOException {
        Services category = servicesRepository.findById(tenderDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Tender tender = new Tender();
        tender.setTitle(tenderDTO.getTitle());
        tender.setDescription(tenderDTO.getDescription());
        tender.setLocation(tenderDTO.getLocation());
        tender.setClosingDate(tenderDTO.getClosingDate());
        tender.setContactInfo(tenderDTO.getContactInfo());
        tender.setCategory(category);
        tender.setStatus(tenderDTO.getStatus() != null ? tenderDTO.getStatus() : TenderStatus.OPEN);
        tender.setDatePosted(LocalDateTime.now());

        if (tenderDTO.getFile() != null) {
            String filePath = fileStorageService.storeFile(tenderDTO.getFile());
            tender.setDocumentPath(filePath);
        }

        tenderRepository.save(tender);
        return "Tender added successfully";
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
            tender.setCategory(category);
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
}
