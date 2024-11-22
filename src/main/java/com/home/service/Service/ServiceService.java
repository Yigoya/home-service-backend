package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.home.service.dto.ServiceDTO;
import com.home.service.dto.TechnicianProfileDTO;
import com.home.service.dto.admin.ServiceCategoryWithServicesDTO;
import com.home.service.dto.admin.ServiceWithCountsDTO;
import com.home.service.models.ServiceCategory;
import com.home.service.models.Services;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.TechnicianRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import com.home.service.models.Technician;

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

    public List<ServiceDTO> getAllServices() {
        return serviceRepository.findAll().stream().map(service -> new ServiceDTO(service))
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> getServiceById(Long id) {
        Services service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        List<Technician> technicians = technicianRepository.findTechniciansByServiceId(id);
        List<TechnicianProfileDTO> technicianDTOs = technicians.stream().map(technician -> {
            TechnicianProfileDTO dto = new TechnicianProfileDTO();
            dto.setId(technician.getId());
            dto.setName(technician.getUser().getName());
            dto.setEmail(technician.getUser().getEmail());
            dto.setPhoneNumber(technician.getUser().getPhoneNumber());
            dto.setBio(technician.getBio());
            dto.setIdCardImage(technician.getIdCardImage());
            dto.setServices(technician.getServices().stream().map(Services::getName).collect(Collectors.toSet()));
            dto.setRating(technician.getRating());
            dto.setCompletedJobs(technician.getCompletedJobs());
            return dto;
        }).collect(Collectors.toList());
        return Map.of("service", service, "technicians", technicianDTOs);
    }

    public Services saveService(Services service) {
        ServiceCategory category = serviceCategoryService.getServiceCategoryById(service.getCategoryId()).orElse(null);
        service.setCategory(category);
        return serviceRepository.save(service);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    public Services updateService(Long id, Services updatedService) {
        Services existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        existingService.setName(updatedService.getName());
        existingService.setDescription(updatedService.getDescription());
        // existingService.setPrice(updatedService.getPrice());
        // existingService.setDuration(updatedService.getDuration());
        existingService.setCategory(
                serviceCategoryService.getServiceCategoryById(updatedService.getCategoryId()).orElse(null));

        return serviceRepository.save(existingService);
    }

    public List<ServiceCategoryWithServicesDTO> getAllServicesCategorized() {
        List<ServiceCategory> categories = serviceCategoryRepository.findAll();
        return categories.stream().map(this::convertToServiceCategoryWithServicesDTO).collect(Collectors.toList());
    }

    private ServiceCategoryWithServicesDTO convertToServiceCategoryWithServicesDTO(ServiceCategory category) {
        ServiceCategoryWithServicesDTO dto = new ServiceCategoryWithServicesDTO();
        dto.setCategoryId(category.getId());
        dto.setCategoryName(category.getCategoryName());
        dto.setDescription(category.getDescription());

        List<ServiceWithCountsDTO> serviceDTOs = serviceRepository.findByCategory(category).stream()
                .map(this::convertToServiceWithCountsDTO)
                .collect(Collectors.toList());

        dto.setServices(serviceDTOs);
        return dto;
    }

    private ServiceWithCountsDTO convertToServiceWithCountsDTO(Services service) {
        ServiceWithCountsDTO dto = new ServiceWithCountsDTO();
        dto.setServiceId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setEstimatedDuration(service.getEstimatedDuration());
        dto.setServiceFee(service.getServiceFee());
        dto.setTechnicianCount(serviceRepository.countTechniciansByServiceId(service.getId()));
        dto.setBookingCount(serviceRepository.countBookingsByServiceId(service.getId()));
        return dto;
    }

}
