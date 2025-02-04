package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
import org.apache.poi.ss.usermodel.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import com.home.service.models.Technician;
import com.home.service.models.enums.EthiopianLanguage;

import java.io.InputStream;
import java.time.LocalTime;
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
        public void uploadServicesFromExcel(MultipartFile file) {
                try {
                        InputStream is = file.getInputStream();
                        Workbook workbook = WorkbookFactory.create(is);
                        System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");
                        Sheet sheet = workbook.getSheetAt(0);

                        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                                Row row = sheet.getRow(rowIndex);
                                System.out.println("Row: " + rowIndex);
                                if (row == null || row.getCell(0) == null || row.getCell(1) == null
                                                || row.getCell(2) == null || row.getCell(3) == null
                                                || row.getCell(4) == null || row.getCell(5) == null
                                                || row.getCell(6) == null)
                                        continue;

                                String categoryIdStr = getCellValueAsString(row.getCell(6));
                                if (categoryIdStr.isEmpty())
                                        continue;
                                System.out.println(categoryIdStr);
                                System.out.println(categoryIdStr.isEmpty());
                                if (categoryIdStr.isEmpty()) {
                                        throw new IllegalArgumentException("Category ID cannot be empty");
                                }
                                System.out.println(row.getCell(0) + " " + row.getCell(1) + " " + row.getCell(2) + " "
                                                + row.getCell(3) + " " + row.getCell(4) + " " + row.getCell(5) + " "
                                                + row.getCell(6));
                                // Retrieve values from each column based on the expected order
                                String englishName = getCellValueAsString(row.getCell(0));
                                String englishDescription = getCellValueAsString(row.getCell(1));
                                String amharicName = getCellValueAsString(row.getCell(2));
                                String amharicDescription = getCellValueAsString(row.getCell(3));
                                String oromoName = getCellValueAsString(row.getCell(4));
                                String oromoDescription = getCellValueAsString(row.getCell(5));

                                Long categoryId = Long.parseLong(categoryIdStr);

                                // Retrieve the service category
                                ServiceCategory category = serviceCategoryService.getServiceCategoryById(categoryId)
                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                "Service Category not found"));

                                // Create the service (additional fields such as estimated duration, service
                                // fee, etc. can be added as needed)
                                Services service = new Services();
                                service.setCategory(category);
                                // If you need to store an icon, use your fileStorageService. For now, we'll set
                                // it as null.
                                service.setIcon(null);
                                service.setEstimatedDuration(LocalTime.of(2, 30));
                                service.setServiceFee(100.0);

                                // Create English translation
                                ServiceTranslation englishTranslation = new ServiceTranslation();
                                englishTranslation.setLang(EthiopianLanguage.ENGLISH);
                                englishTranslation.setName(englishName);
                                englishTranslation.setDescription(englishDescription);
                                englishTranslation.setService(service);

                                // Create Amharic translation
                                ServiceTranslation amharicTranslation = new ServiceTranslation();
                                amharicTranslation.setLang(EthiopianLanguage.AMHARIC);
                                amharicTranslation.setName(amharicName);
                                amharicTranslation.setDescription(amharicDescription);
                                amharicTranslation.setService(service);

                                // Create Oromo translation
                                ServiceTranslation oromoTranslation = new ServiceTranslation();
                                oromoTranslation.setLang(EthiopianLanguage.OROMO); // Adjust language code as needed
                                oromoTranslation.setName(oromoName);
                                oromoTranslation.setDescription(oromoDescription);
                                oromoTranslation.setService(service);

                                // Add translations to the service (assuming a bidirectional mapping and cascade
                                // persist is set up)

                                Services savedServices = serviceRepository.save(service);
                                englishTranslation.setService(savedServices);

                                amharicTranslation.setService(savedServices);
                                oromoTranslation.setService(savedServices);
                                savedServices.getTranslations()
                                                .add(serviceTranslationRepository.save(englishTranslation));
                                savedServices.getTranslations()
                                                .add(serviceTranslationRepository.save(amharicTranslation));
                                savedServices.getTranslations()
                                                .add(serviceTranslationRepository.save(oromoTranslation));
                                serviceRepository.save(savedServices);
                        }

                } catch (Exception e) {
                        System.out.println("Failed to upload services from Excel: " + e.getMessage());
                        System.out.println("Failed to upload services from Excel: " + e);
                        e.printStackTrace();
                        throw new IllegalArgumentException("Failed to upload services from Excel");
                }
        }

        // Helper method to safely get a cell's value as a String
        private String getCellValueAsString(Cell cell) {
                if (cell == null)
                        return "";
                switch (cell.getCellType()) {
                        case STRING:
                                return cell.getStringCellValue().trim();
                        case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                        return cell.getDateCellValue().toString();
                                } else {
                                        // Remove any trailing ".0" if not needed
                                        double value = cell.getNumericCellValue();
                                        return (value == Math.floor(value)) ? String.valueOf((long) value)
                                                        : String.valueOf(value);
                                }
                        case BOOLEAN:
                                return Boolean.toString(cell.getBooleanCellValue());
                        case FORMULA:
                                return cell.getCellFormula();
                        default:
                                return "";
                }
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
                                                : service.getMobileCategory());
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

        public List<ServiceCategoryWithServicesDTO> getAllServicesCategorized(EthiopianLanguage lang) {
                List<ServiceCategory> categories = serviceCategoryRepository.findAll();
                return categories.stream().map(category -> convertToServiceCategoryWithServicesDTO(category, lang))
                                .collect(Collectors.toList());
        }

        private ServiceCategoryWithServicesDTO convertToServiceCategoryWithServicesDTO(ServiceCategory category,
                        EthiopianLanguage lang) {
                ServiceCategoryWithServicesDTO dto = new ServiceCategoryWithServicesDTO();
                dto.setCategoryId(category.getId());
                dto.setCategoryName(category.getTranslations().stream()
                                .filter(t -> t.getLang().equals(lang))
                                .findFirst()
                                .orElse(category.getTranslations().stream()
                                                .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                                                .findFirst().get())
                                .getName());
                dto.setDescription(category.getTranslations().stream()
                                .filter(t -> t.getLang().equals(lang))
                                .findFirst()
                                .orElse(category.getTranslations().stream()
                                                .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                                                .findFirst().get())
                                .getDescription());
                dto.setIcon(category.getIcon());
                List<ServiceWithCountsDTO> serviceDTOs = serviceRepository.findByCategory(category).stream()
                                .map(service -> this.convertToServiceWithCountsDTO(service, lang))
                                .collect(Collectors.toList());

                dto.setServices(serviceDTOs);
                return dto;
        }

        private ServiceWithCountsDTO convertToServiceWithCountsDTO(Services service, EthiopianLanguage lang) {
                ServiceWithCountsDTO dto = new ServiceWithCountsDTO();
                dto.setServiceId(service.getId());
                dto.setName(service.getTranslations().stream()
                                .filter(t -> t.getLang().equals(lang))
                                .findFirst()
                                .orElse(service.getTranslations().stream()
                                                .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                                                .findFirst().get())
                                .getName());
                dto.setDescription(service.getTranslations().stream()
                                .filter(t -> t.getLang().equals(lang))
                                .findFirst()
                                .orElse(service.getTranslations().stream()
                                                .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                                                .findFirst().get())
                                .getDescription());
                dto.setEstimatedDuration(service.getEstimatedDuration());
                dto.setServiceFee(service.getServiceFee());
                dto.setTechnicianCount(serviceRepository.countTechniciansByServiceId(service.getId()));
                dto.setBookingCount(serviceRepository.countBookingsByServiceId(service.getId()));
                dto.setIcon(service.getIcon());
                return dto;
        }

}
