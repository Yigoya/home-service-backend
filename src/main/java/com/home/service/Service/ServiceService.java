package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.dto.ServiceDTO;
import com.home.service.dto.ServiceImportDTO;
import com.home.service.dto.ServiceLangRequest;
import com.home.service.dto.ServiceRequest;
import com.home.service.dto.TechnicianProfileDTO;
import com.home.service.dto.admin.ServiceCategoryWithServicesDTO;
import com.home.service.dto.admin.ServiceWithCountsDTO;
import com.home.service.models.ServiceCategory;
import com.home.service.models.ServiceCategoryTranslation;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.Services;
import com.home.service.repositories.ServiceCategoryRepository;
import com.home.service.repositories.ServiceCategoryTranslationRepository;
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
import java.util.HashMap;
import java.util.Comparator;
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
        private ServiceCategoryTranslationRepository serviceCategoryTranslationRepository;

        @Autowired
        private FileStorageService fileStorageService;

        public void importServices(List<ServiceImportDTO> services, Map<String, String> iconMap) {
                Map<Integer, Services> levelToServiceMap = new HashMap<>();
                ServiceCategory currentCategory = null;

                for (ServiceImportDTO dto : services) {
                        // Provide safe placeholders to satisfy non-null constraints
                        String enName = defaultIfBlank(dto.getNameEnglish(), "-");
                        String enDesc = defaultIfBlank(dto.getDescriptionEnglish(), "-");
                        String amName = defaultIfBlank(dto.getNameAmharic(), enName);
                        String amDesc = defaultIfBlank(dto.getDescriptionAmharic(), enDesc);
                        String omName = defaultIfBlank(dto.getNameOromo(), enName);
                        String omDesc = defaultIfBlank(dto.getDescriptionOromo(), enDesc);
                        String tiName = defaultIfBlank(dto.getNameTigrinya(), enName);
                        String tiDesc = defaultIfBlank(dto.getDescriptionTigrinya(), enDesc);
                        String soName = defaultIfBlank(dto.getNameSomali(), enName);
                        String soDesc = defaultIfBlank(dto.getDescriptionSomali(), enDesc);

                        if (dto.getLevel() == 0) {
                                // Handle category
                                currentCategory = new ServiceCategory();
                                currentCategory.setIsMobileCategory(false); // Default value

                                // Set icon if available
                                if (iconMap != null && !iconMap.isEmpty() && dto.getIconFileName() != null
                                                && iconMap.containsKey(dto.getIconFileName())) {
                                        currentCategory.setIcon(iconMap.get(dto.getIconFileName()));
                                }

                                // Add translations
                                ServiceCategoryTranslation translationEnglish = new ServiceCategoryTranslation();
                                translationEnglish.setName(enName);
                                translationEnglish.setDescription(enDesc);
                                translationEnglish.setLang(EthiopianLanguage.ENGLISH);

                                ServiceCategoryTranslation translationAmharic = new ServiceCategoryTranslation();
                                translationAmharic.setName(amName);
                                translationAmharic.setDescription(amDesc);
                                translationAmharic.setLang(EthiopianLanguage.AMHARIC);

                                ServiceCategoryTranslation translationOromo = new ServiceCategoryTranslation();
                                translationOromo.setName(omName);
                                translationOromo.setDescription(omDesc);
                                translationOromo.setLang(EthiopianLanguage.OROMO);

                                ServiceCategoryTranslation translationTigrinya = new ServiceCategoryTranslation();
                                translationTigrinya.setName(tiName);
                                translationTigrinya.setDescription(tiDesc);
                                translationTigrinya.setLang(EthiopianLanguage.TIGRINYA);

                                ServiceCategoryTranslation translationSomali = new ServiceCategoryTranslation();
                                translationSomali.setName(soName);
                                translationSomali.setDescription(soDesc);
                                translationSomali.setLang(EthiopianLanguage.SOMALI);

                                ServiceCategory savedCategory = serviceCategoryRepository
                                                .save(currentCategory);
                                translationEnglish.setCategory(savedCategory);
                                translationAmharic.setCategory(savedCategory);
                                translationOromo.setCategory(savedCategory);
                                translationTigrinya.setCategory(savedCategory);
                                translationSomali.setCategory(savedCategory);

                                savedCategory.getTranslations()
                                                .add(serviceCategoryTranslationRepository.save(translationEnglish));
                                savedCategory.getTranslations()
                                                .add(serviceCategoryTranslationRepository.save(translationAmharic));
                                savedCategory.getTranslations()
                                                .add(serviceCategoryTranslationRepository.save(translationOromo));
                                savedCategory.getTranslations()
                                                .add(serviceCategoryTranslationRepository.save(translationTigrinya));
                                savedCategory.getTranslations()
                                                .add(serviceCategoryTranslationRepository.save(translationSomali));

                        } else {
                                // Handle service
                                Services service = new Services();
                                service.setCategory(currentCategory); // Link to the current category

                                // Set icon if available
                                if (iconMap != null && !iconMap.isEmpty() && dto.getIconFileName() != null
                                                && iconMap.containsKey(dto.getIconFileName())) {
                                        service.setIcon(iconMap.get(dto.getIconFileName()));
                                }

                                // Add translations
                                ServiceTranslation translationEnglish = new ServiceTranslation();
                                translationEnglish.setName(enName);
                                translationEnglish.setDescription(enDesc);
                                translationEnglish.setLang(EthiopianLanguage.ENGLISH);

                                ServiceTranslation translationAmharic = new ServiceTranslation();
                                translationAmharic.setName(amName);
                                translationAmharic.setDescription(amDesc);
                                translationAmharic.setLang(EthiopianLanguage.AMHARIC);

                                ServiceTranslation translationOromo = new ServiceTranslation();
                                translationOromo.setName(omName);
                                translationOromo.setDescription(omDesc);
                                translationOromo.setLang(EthiopianLanguage.OROMO);

                                ServiceTranslation translationTigrinya = new ServiceTranslation();
                                translationTigrinya.setName(tiName);
                                translationTigrinya.setDescription(tiDesc);
                                translationTigrinya.setLang(EthiopianLanguage.TIGRINYA);

                                ServiceTranslation translationSomali = new ServiceTranslation();
                                translationSomali.setName(soName);
                                translationSomali.setDescription(soDesc);
                                translationSomali.setLang(EthiopianLanguage.SOMALI);

                                service = serviceRepository.save(service);
                                levelToServiceMap.put(dto.getLevel(), service);
                                translationEnglish.setService(service);
                                translationAmharic.setService(service);
                                translationOromo.setService(service);
                                translationTigrinya.setService(service);
                                translationSomali.setService(service);
                                service.getTranslations()
                                                .add(serviceTranslationRepository.save(translationEnglish));
                                service.getTranslations()
                                                .add(serviceTranslationRepository.save(translationAmharic));
                                service.getTranslations()
                                                .add(serviceTranslationRepository.save(translationOromo));
                                service.getTranslations()
                                                .add(serviceTranslationRepository.save(translationTigrinya));
                                service.getTranslations()
                                                .add(serviceTranslationRepository.save(translationSomali));
                                serviceRepository.save(service);

                                // Handle nested services
                                if (dto.getLevel() > 1) {
                                        Services parentService = levelToServiceMap.get(dto.getLevel() - 1);
                                        parentService.getServices().add(service);
                                        serviceRepository.save(parentService);
                                }
                        }
                }
        }

        private String defaultIfBlank(String value, String defaultValue) {
                if (value == null)
                        return defaultValue;
                String trimmed = value.trim();
                return trimmed.isEmpty() ? defaultValue : trimmed;
        }

        public List<ServiceDTO> getAllServices(EthiopianLanguage lang) {
                return serviceRepository.findByServiceIdIsNullOrderByIdAsc().stream()
                                .map(service -> new ServiceDTO(service, lang))
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

                if (serviceRequest.getDocument() != null) {
                        String document = fileStorageService.storeFile(serviceRequest.getDocument());
                        service.setDocument(document);
                }

                // Create the translation and link it to the service
                ServiceTranslation translation = new ServiceTranslation();
                translation.setLang(serviceRequest.getLang());
                translation.setName(serviceRequest.getName());
                translation.setDescription(serviceRequest.getDescription());
                translation.setService(service);

                // Add translation to service
                service.getTranslations().add(translation);

                if (serviceRequest.getServiceId() != null) {
                        Services parentService = serviceRepository.findById(serviceRequest.getServiceId())
                                        .orElseThrow(() -> new EntityNotFoundException("Service not found"));
                        parentService.getServices().add(service);
                        serviceRepository.save(parentService);
                } else {
                        serviceRepository.save(service);
                }

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
                List<ServiceCategory> categories = serviceCategoryRepository.findAll()
                                                .stream()
                                                // .filter(category -> !category.getId().equals(5L)
                                                //                 && !category.getId().equals(7L)
                                                //                 && !category.getId().equals(8L))
                                                .sorted(Comparator
                                                                .comparing(ServiceCategory::getOrder,
                                                                                Comparator.nullsLast(Long::compareTo))
                                                                .thenComparing(ServiceCategory::getId))
                                                .collect(Collectors.toList());
                return categories.stream().map(category -> convertToServiceCategoryWithServicesDTO(category, lang))
                                .collect(Collectors.toList());
        }

        private ServiceCategoryWithServicesDTO convertToServiceCategoryWithServicesDTO(ServiceCategory category,
                        EthiopianLanguage lang) {
                ServiceCategoryWithServicesDTO dto = new ServiceCategoryWithServicesDTO();
                Long categoryOrder = category.getOrder() != null ? category.getOrder() : category.getId();
                // Downstream clients read the legacy categoryId field to obtain ordering info.
                dto.setCategoryId(categoryOrder);
                dto.setOrder(category.getOrder());
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
                List<ServiceWithCountsDTO> serviceDTOs = serviceRepository
                                .findByCategoryOrderByDisplayOrderAsc(category)
                                .stream()
                                .filter(service -> service.getServiceId() == null)
                                .sorted(Comparator
                                                .comparing(Services::getDisplayOrder,
                                                                Comparator.nullsLast(Long::compareTo))
                                                .thenComparing(Services::getId))
                                .map(service -> this.convertToServiceWithCountsDTO(service, lang))
                                .collect(Collectors.toList());
                // serviceDTOs = serviceDTOs.stream()
                // .filter(serviceDTO -> serviceDTO.getServiceId() == null)
                // .collect(Collectors.toList());
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
                dto.setDocument(service.getDocument());
                dto.setCategoryId(service.getCategory().getId());
                dto.setServices(service.getServices().stream()
                                .sorted(Comparator
                                                .comparing(Services::getDisplayOrder,
                                                                Comparator.nullsLast(Long::compareTo))
                                                .thenComparing(Services::getId))
                                .map(s -> convertToServiceWithCountsDTO(s, lang))
                                .collect(Collectors.toList()));
                return dto;
        }

        @Transactional
        public List<ServiceDTO> getServicesByServiceId(Long serviceId, EthiopianLanguage lang) {
                Services parentService = serviceRepository.findById(serviceId)
                                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
                return parentService.getServices().stream()
                                .map(service -> new ServiceDTO(service, lang))
                                .collect(Collectors.toList());
        }

        public void addIconsToServices(Map<Long, MultipartFile> serviceIcons) {
                serviceIcons.forEach((serviceId, iconFile) -> {
                        Services service = serviceRepository.findById(serviceId)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "Service not found with id: " + serviceId));
                        String iconPath = fileStorageService.storeFile(iconFile);
                        service.setIcon(iconPath);
                        serviceRepository.save(service);
                });
        }

}
