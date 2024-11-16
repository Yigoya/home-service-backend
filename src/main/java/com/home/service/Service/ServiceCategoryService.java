package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.home.service.dto.ServiceCategoryWithServicesDTO;
import com.home.service.dto.ServiceDTO;
import com.home.service.models.ServiceCategory;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceCategoryService {
    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    public List<ServiceCategory> getAllServiceCategories() {
        return serviceCategoryRepository.findAll();
    }

    public Optional<ServiceCategory> getServiceCategoryById(Long id) {
        return serviceCategoryRepository.findById(id);
    }

    public Optional<ServiceCategoryWithServicesDTO> getServiceCategoryWithServicesById(Long id) {
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service Category not found"));
        List<ServiceDTO> services = serviceRepository.findByCategory(serviceCategory).stream()
                .map(ServiceDTO::new)
                .toList();

        return Optional.of(new ServiceCategoryWithServicesDTO(serviceCategory, services));
    }

    public ServiceCategory saveServiceCategory(ServiceCategory serviceCategory) {
        return serviceCategoryRepository.save(serviceCategory);
    }

    public void deleteServiceCategory(Long id) {
        serviceCategoryRepository.deleteById(id);
    }
}
