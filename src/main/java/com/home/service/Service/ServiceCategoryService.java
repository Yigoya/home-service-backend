package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.home.service.dto.ServiceCatagoryRequest;
import com.home.service.dto.ServiceCategoryDTO;
import com.home.service.dto.ServiceCategoryWithServicesDTO;
import com.home.service.dto.ServiceDTO;
import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceCategoryTranslation;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceCategoryTranslationRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.ServiceTranslationRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServiceCategoryService {
    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceCategoryTranslationRepository serviceCategoryTranslationRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public List<ServiceCategoryDTO> getAllServiceCategories(EthiopianLanguage lang, boolean isMobileCategory) {
        return serviceCategoryRepository.findAll().stream()
                .filter(serviceCategory -> Boolean.TRUE
                        .equals(serviceCategory.getIsMobileCategory()) == isMobileCategory)
                .map(serviceCategory -> new ServiceCategoryDTO(serviceCategory, lang))
                .collect(Collectors.toList());
    }

    public Optional<ServiceCategory> getServiceCategoryById(Long id) {
        return serviceCategoryRepository.findById(id);
    }

    public Optional<ServiceCategoryWithServicesDTO> getServiceCategoryWithServicesById(Long id,
            EthiopianLanguage lang) {
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service Category not found"));
        List<ServiceDTO> services = serviceRepository.findByCategory(serviceCategory).stream()
                .map(service -> new ServiceDTO(service, lang))
                .toList();

        return Optional.of(new ServiceCategoryWithServicesDTO(serviceCategory, services));
    }

    public String saveServiceCategory(ServiceCatagoryRequest serviceCategoryRequest) {
        // Create a new ServiceCategory instance
        ServiceCategory category = new ServiceCategory();

        // Map and add translations before saving
        ServiceCategoryTranslation translation = new ServiceCategoryTranslation();
        translation.setName(serviceCategoryRequest.getName());
        translation.setDescription(serviceCategoryRequest.getDescription());
        translation.setLang(serviceCategoryRequest.getLang());
        translation.setCategory(category);

        // Add the translation to the category
        category.getTranslations().add(translation);
        String icon = fileStorageService.storeFile(serviceCategoryRequest.getIcon());
        category.setIcon(icon);
        category.setIsMobileCategory(serviceCategoryRequest.getIsMobileCategory());

        // Save the category (translations will be saved automatically due to cascading)
        serviceCategoryRepository.save(category);

        return "Service Category saved successfully";
    }

    public String updateServiceCategory(Long id, ServiceCatagoryRequest serviceCategory) {
        ServiceCategory category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service Category not found"));
        Optional<ServiceCategoryTranslation> existingTranslation = serviceCategoryTranslationRepository
                .findByCategoryAndLang(category,
                        serviceCategory.getLang());

        if (existingTranslation.isPresent()) {
            ServiceCategoryTranslation translation = existingTranslation.get();
            translation.setName(serviceCategory.getName());
            translation.setDescription(serviceCategory.getDescription());
        } else {
            ServiceCategoryTranslation translation = new ServiceCategoryTranslation();
            translation.setName(serviceCategory.getName());
            translation.setDescription(serviceCategory.getDescription());
            translation.setLang(serviceCategory.getLang());
            translation.setCategory(category);
            category.getTranslations().add(translation);
        }

        String icon = fileStorageService.storeFile(serviceCategory.getIcon());
        category.setIcon(icon);
        category.setIsMobileCategory(serviceCategory.getIsMobileCategory() == null ? category.getIsMobileCategory()
                : serviceCategory.getIsMobileCategory());
        serviceCategoryRepository.save(category);
        return "Service Category updated successfully";
    }

    public String addServiceCategoryLanguage(Long id, ServiceCatagoryRequest serviceCategoryRequest) {
        // Fetch the existing ServiceCategory
        ServiceCategory category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service Category not found"));

        // Check if a translation for the given language already exists
        Optional<ServiceCategoryTranslation> existingTranslation = serviceCategoryTranslationRepository
                .findByCategoryAndLang(category, serviceCategoryRequest.getLang());

        if (existingTranslation.isPresent()) {
            // Update the existing translation
            ServiceCategoryTranslation translation = existingTranslation.get();
            translation.setName(serviceCategoryRequest.getName());
            translation.setDescription(serviceCategoryRequest.getDescription());
            category.getTranslations().add(translation);
        } else {
            // Create a new ServiceCategoryTranslation
            ServiceCategoryTranslation translation = new ServiceCategoryTranslation();
            translation.setName(serviceCategoryRequest.getName());
            translation.setDescription(serviceCategoryRequest.getDescription());
            translation.setLang(serviceCategoryRequest.getLang());
            translation.setCategory(category); // Link the translation to the category

            // Add the translation to the category's translations
            category.getTranslations().add(translation);
        }

        // Save the updated category (cascade will handle saving the translation)
        serviceCategoryRepository.save(category);
        return "Language added or updated successfully";
    }

    public void deleteServiceCategory(Long id) {
        serviceCategoryRepository.deleteById(id);
    }
}
