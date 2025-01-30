package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.home.service.dto.ServiceDTO;
import com.home.service.dto.ServiceLangRequest;
import com.home.service.dto.ServiceRequest;
import com.home.service.dto.TechnicianProfileDTO;
import com.home.service.dto.admin.ServiceCategoryWithServicesDTO;
import com.home.service.dto.admin.ServiceWithCountsDTO;
import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.Services;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.ServiceTranslationRepository;
import com.home.service.repositories.TechnicianRepository;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import com.home.service.models.Technician;
import com.home.service.models.enums.EthiopianLanguage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceService {
        @Autowired
        private ServiceRepository serviceRepository;

        @Autowired
        private ServiceCategoryService serviceCategoryService;

        @Autowired
        private TechnicianRepository technicianRepository;

        @Autowired
        private ServiceCategoryRepository serviceCategoryRepository;

        @Autowired
        private ServiceTranslationRepository serviceTranslationRepository;

        @Autowired
        private FileStorageService fileStorageService;

        public List<ServiceDTO> getAllServices(EthiopianLanguage lang) {
                return serviceRepository.findAll().stream().map(service -> new ServiceDTO(service, lang))
                                .collect(Collectors.toList());
        }

        @Transactional
        public Map<String, Object> getServiceById(Long id, EthiopianLanguage lang) {
                Services service = serviceRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
                ServiceDTO serviceDTO = new ServiceDTO(service, lang);
                List<Technician> technicians = technicianRepository.findTechniciansByServiceId(id);
                List<TechnicianProfileDTO> technicianDTOs = technicians.stream()
                                .map(technician -> new TechnicianProfileDTO(technician, lang))
                                .collect(Collectors.toList());
                return Map.of("service", serviceDTO, "technicians", technicianDTOs);
        }

        @Transactional
        public String saveService(ServiceRequest serviceRequest) {
                ServiceCategory category = serviceCategoryService.getServiceCategoryById(serviceRequest.getCategoryId())
                                .orElseThrow(() -> new EntityNotFoundException("Service Category not found"));

                // Create and save the service
                Services service = new Services();
                service.setCategory(category);
                service.setEstimatedDuration(serviceRequest.getEstimatedDuration());
                service.setServiceFee(serviceRequest.getServiceFee());
                service.setMobileCategory(
                                serviceRequest.getMobileCategoryId() != null ? serviceCategoryService
                                                .getServiceCategoryById(serviceRequest.getMobileCategoryId()).get()
                                                : null);
                String icon = fileStorageService.storeFile(serviceRequest.getIcon());
                service.setIcon(icon);

                // Create the translation and link it to the service
                ServiceTranslation translation = new ServiceTranslation();
                translation.setLang(serviceRequest.getLang());
                translation.setName(serviceRequest.getName());
                translation.setDescription(serviceRequest.getDescription());
                translation.setService(service);

                // Add translation to service
                service.getTranslations().add(translation);

                // Save the service and cascade save translations
                serviceRepository.save(service);

                return "Service saved successfully";
        }

        @Transactional
        public String updateService(Long id, ServiceRequest serviceRequest) {
                Services service = serviceRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

                // Check if translation for the given language already exists
                ServiceTranslation translation = service.getTranslations().stream()
                                .filter(t -> t.getLang().equals(serviceRequest.getLang()))
                                .findFirst()
                                .orElse(new ServiceTranslation());

                // Update or set new translation details
                translation.setLang(serviceRequest.getLang());
                translation.setName(serviceRequest.getName());
                translation.setDescription(serviceRequest.getDescription());
                translation.setService(service); // Set the service reference

                // Add translation to service if it's new
                if (!service.getTranslations().contains(translation)) {
                        service.getTranslations().add(translation);
                }

                service.setId(id);
                service.setCategory(
                                serviceCategoryService.getServiceCategoryById(serviceRequest.getCategoryId()).get());
                service.setMobileCategory(
                                serviceRequest.getMobileCategoryId() != null ? serviceCategoryService
                                                .getServiceCategoryById(serviceRequest.getMobileCategoryId()).get()
                                                : null);
                service.setEstimatedDuration(serviceRequest.getEstimatedDuration());
                service.setServiceFee(serviceRequest.getServiceFee());
                String icon = fileStorageService.storeFile(serviceRequest.getIcon());
                service.setIcon(icon);

                serviceRepository.save(service);
                return "Service updated successfully";
        }

        @Transactional
        public String addServiceLanguage(Long id, ServiceLangRequest serviceRequest) {
                Services service = serviceRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

                // Check if translation for the given language already exists
                ServiceTranslation translation = service.getTranslations().stream()
                                .filter(t -> t.getLang().equals(serviceRequest.getLang()))
                                .findFirst()
                                .orElse(new ServiceTranslation());

                // Update or set new translation details
                translation.setLang(serviceRequest.getLang());
                translation.setName(serviceRequest.getName());
                translation.setDescription(serviceRequest.getDescription());
                translation.setService(service);

                // Add translation to service if it's new
                if (!service.getTranslations().contains(translation)) {
                        service.getTranslations().add(translation);
                }

                serviceRepository.save(service);

                return "Language " + serviceRequest.getLang() + " added/updated successfully";
        }

        public void deleteService(Long id) {
                serviceRepository.deleteById(id);
        }

        public List<ServiceCategoryWithServicesDTO> getAllServicesCategorized() {
                List<ServiceCategory> categories = serviceCategoryRepository.findAll();
                return categories.stream().map(this::convertToServiceCategoryWithServicesDTO)
                                .collect(Collectors.toList());
        }

        private ServiceCategoryWithServicesDTO convertToServiceCategoryWithServicesDTO(ServiceCategory category) {
                ServiceCategoryWithServicesDTO dto = new ServiceCategoryWithServicesDTO();
                dto.setCategoryId(category.getId());
                dto.setCategoryName(category.getTranslations().stream()
                                .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                                .findFirst().get().getName());
                dto.setDescription(category.getTranslations().stream()
                                .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                                .findFirst().get().getDescription());

                List<ServiceWithCountsDTO> serviceDTOs = serviceRepository.findByCategory(category).stream()
                                .map(this::convertToServiceWithCountsDTO)
                                .collect(Collectors.toList());

                dto.setServices(serviceDTOs);
                return dto;
        }

        private ServiceWithCountsDTO convertToServiceWithCountsDTO(Services service) {
                ServiceWithCountsDTO dto = new ServiceWithCountsDTO();
                dto.setServiceId(service.getId());
                dto.setName(service.getTranslations().stream()
                                .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                                .findFirst().get().getName());
                dto.setDescription(service.getTranslations().stream()
                                .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                                .findFirst().get().getDescription());
                dto.setEstimatedDuration(service.getEstimatedDuration());
                dto.setServiceFee(service.getServiceFee());
                dto.setTechnicianCount(serviceRepository.countTechniciansByServiceId(service.getId()));
                dto.setBookingCount(serviceRepository.countBookingsByServiceId(service.getId()));
                return dto;
        }

}
