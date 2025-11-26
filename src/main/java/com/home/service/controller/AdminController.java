package com.home.service.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.BookingService;
import com.home.service.Service.CustomerService;
import com.home.service.Service.DisputeService;
import com.home.service.Service.ServiceService;
import com.home.service.Service.TechnicianService;
import com.home.service.Service.UserService;
import com.home.service.dto.CustomerProfileDTO;
import com.home.service.dto.DisputeDTO;
import com.home.service.dto.OperatorProfileDTO;
import com.home.service.dto.ServiceCatagoryRequest;
import com.home.service.dto.ServiceImportDTO;
import com.home.service.dto.ServiceLangRequest;
import com.home.service.dto.ServiceRequest;
import com.home.service.dto.TechnicianProfileDTO;
import com.home.service.dto.admin.BookingDetailDTO;
import com.home.service.dto.admin.CustomerDetailDTO;
import com.home.service.dto.admin.DisputeDetailDTO;
import com.home.service.dto.admin.ServiceCategoryWithServicesDTO;
import com.home.service.dto.admin.TechnicianDetailDTO;
import com.home.service.repositories.DisputeRepository;
import com.home.service.repositories.TechnicianRepository;
import com.home.service.services.EmailService;
import com.home.service.services.FileStorageService;
import com.home.service.models.enums.AccountStatus;
import com.home.service.models.enums.BookingStatus;
import com.home.service.models.enums.DisputeStatus;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.Service.OperatorService;
import com.home.service.Service.PaymentProofService;
import com.home.service.Service.QuestionService;
import com.home.service.Service.ServiceCategoryService;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.EnumSet;
import jakarta.persistence.EntityManagerFactory;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import com.home.service.models.Dispute;
import com.home.service.models.QuestionRequest;
import com.home.service.models.Technician;
import com.home.service.models.TechnicianProofResponse;
import com.home.service.models.User;

@CrossOrigin(originPatterns = "*")
@RestController
@RequestMapping("/admin")
public class AdminController {

        @Autowired
        private TechnicianRepository technicianRepository;

        @Autowired
        private EmailService emailService;

        @Autowired
        private DisputeRepository disputeRepository;

        @Autowired
        private ServiceService serviceService;

        @Autowired
        private CustomerService customerService;

        @Autowired
        private TechnicianService technicianService;

        @Autowired
        private OperatorService operatorService;

        @Autowired
        private BookingService bookingService;

        @Autowired
        private DisputeService disputeService;

        @Autowired
        private ServiceCategoryService serviceCategoryService;

        @Autowired
        private PaymentProofService paymentProofService;

        @Autowired
        private UserService userService;

        @Autowired
        private QuestionService questionService;

        @Autowired
        private FileStorageService fileStorageService;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Autowired
        private EntityManagerFactory entityManagerFactory;

        private List<ServiceCategoryWithServicesDTO> categoriesEnglish;
        private List<ServiceCategoryWithServicesDTO> categoriesAmharic;
        private List<ServiceCategoryWithServicesDTO> categoriesOromo;

        private boolean isUpdated = false;

        // Expose a light cache invalidation hook so other flows (e.g., bulk import)
        // can refresh the cached categories/services view.
        public void invalidateServicesCache() {
                this.isUpdated = true;
                this.categoriesEnglish = null;
                this.categoriesAmharic = null;
                this.categoriesOromo = null;
        }

        @GetMapping("/services")
        public ResponseEntity<List<ServiceCategoryWithServicesDTO>> getAllServicesCategorized(
                        @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang) {
                if (lang == EthiopianLanguage.AMHARIC) {
                        if (categoriesAmharic == null || isUpdated) {
                                categoriesAmharic = serviceService.getAllServicesCategorized(lang);
                        }
                        return ResponseEntity.ok(categoriesAmharic);
                } else if (lang == EthiopianLanguage.OROMO) {
                        if (categoriesOromo == null || isUpdated) {
                                categoriesOromo = serviceService.getAllServicesCategorized(lang);
                        }
                        return ResponseEntity.ok(categoriesOromo);
                } else {
                        if (categoriesEnglish == null || isUpdated) {
                                categoriesEnglish = serviceService.getAllServicesCategorized(lang);
                        }
                        return ResponseEntity.ok(categoriesEnglish);
                }
        }

        @GetMapping("/unverified-technicians")
        public ResponseEntity<List<TechnicianProfileDTO>> listUnverifiedTechnicians() {
                List<TechnicianProfileDTO> technicianDTOs = technicianService.listUnverifiedTechnicians();
                return ResponseEntity.ok(technicianDTOs);
        }

        @GetMapping("/unverified-technicians/{technicianId}")
        public ResponseEntity<TechnicianProfileDTO> getUnverifiedTechnicianById(@PathVariable Long technicianId) {
                TechnicianProfileDTO technicianDTO = technicianService.getUnverifiedTechnicianById(technicianId);
                return ResponseEntity.ok(technicianDTO);
        }

        @GetMapping("technicians/verify/{technicianId}")
        public ResponseEntity<String> acceptTechnician(@PathVariable Long technicianId) {
                Technician technician = technicianRepository.findById(technicianId)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

                technician.setVerified(true); // Mark technician as verified
                User user = technician.getUser();
                user.setStatus(AccountStatus.ACTIVE);
                technicianRepository.save(technician);


                emailService.sendTechnicianVerificationEmail(technician.getUser());
                return ResponseEntity.ok("Technician verified and verification email sent");
        }

        @GetMapping("/pending-proofs")
        public ResponseEntity<List<TechnicianProofResponse>> getTechniciansWithPendingProofs() {
                List<TechnicianProofResponse> response = paymentProofService.getTechniciansWithPendingProofs();
                return ResponseEntity.ok(response);
        }

        @GetMapping("technicians/decline/{technicianId}")
        public ResponseEntity<String> declineTechnician(@PathVariable Long technicianId) {
                Technician technician = technicianRepository.findById(technicianId)
                                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

                // Send email notification to the technician's user account
                emailService.sendDeclineEmail(technician.getUser());

                // Delete technician record from the database
                technicianRepository.delete(technician);

                return ResponseEntity
                                .ok("Technician application declined, email notification sent, and record removed.");
        }

        @GetMapping("/dispute")
        public ResponseEntity<List<DisputeDTO>> getAllDisputes(@RequestParam(required = false) DisputeStatus status) {
                List<Dispute> disputes = (status == null)
                                ? disputeRepository.findAll()
                                : disputeRepository.findAllByStatus(status);
                System.out.println(disputes.get(0).getDescription());
                List<DisputeDTO> disputeDTOs = disputes.stream()
                                .map(dispute -> new DisputeDTO(
                                                dispute.getId(),
                                                dispute.getCustomer().getUser().getName(),
                                                dispute.getDescription(),
                                                dispute.getReason(),
                                                dispute.getStatus(),
                                                dispute.getCreatedAt(),
                                                dispute.getUpdatedAt()))
                                .collect(Collectors.toList());

                return ResponseEntity.ok(disputeDTOs);
        }

        // Update the status of a specific dispute
        @CrossOrigin(originPatterns = "*")
        @PutMapping("/disputes/{disputeId}")
        public ResponseEntity<String> updateDisputeStatus(
                        @PathVariable Long disputeId,
                        @RequestParam DisputeStatus status) {
                Dispute dispute = disputeRepository.findById(disputeId)
                                .orElseThrow(() -> new EntityNotFoundException("Dispute not found"));

                dispute.setStatus(status);
                disputeRepository.save(dispute);

                return ResponseEntity.ok("Dispute status updated to " + status);
        }

        @CrossOrigin(originPatterns = "*")
        @PutMapping("/services/{id}")
        public ResponseEntity<String> updateService(@PathVariable Long id,
                        @Valid @ModelAttribute ServiceRequest updatedService) {
                serviceService.updateService(id, updatedService);
                return ResponseEntity.ok("Service updated successfully");
        }

        @CrossOrigin(originPatterns = "*")
        @PutMapping("/services/{id}/language")
        public ResponseEntity<String> addServiceLanguage(@PathVariable Long id,
                        @Valid @RequestBody ServiceLangRequest updatedService) {
                String updated = serviceService.addServiceLanguage(id, updatedService);
                return ResponseEntity.ok(updated);
        }

        @PostMapping("/services")
        public String createService(@Valid @ModelAttribute ServiceRequest service) {
                return serviceService.saveService(service);
        }

        @PostMapping("/services/upload")
        public ResponseEntity<String> uploadServices(@RequestParam("file") MultipartFile file) {

                serviceService.uploadServicesFromExcel(file);
                return ResponseEntity.ok("Services uploaded successfully.");

        }

        @PostMapping("/service-categories")
        public String createServiceCategory(@Valid @ModelAttribute ServiceCatagoryRequest serviceCategory) {
                return serviceCategoryService.saveServiceCategory(serviceCategory);
        }

        @PostMapping("/service-categories/{id}/language")
        public String addServiceCategoryLanguage(@PathVariable Long id,
                        @Valid @RequestBody ServiceCatagoryRequest serviceCategory) {
                return serviceCategoryService.addServiceCategoryLanguage(id, serviceCategory);
        }

        @CrossOrigin(originPatterns = "*")
        @PutMapping("/service-categories/{id}")
        public String updateServiceCategory(@PathVariable Long id,
                        @Valid @ModelAttribute ServiceCatagoryRequest serviceCategory) {
                return serviceCategoryService.updateServiceCategory(id, serviceCategory);
        }

        @GetMapping("/customer")
        public ResponseEntity<Page<CustomerProfileDTO>> getAllCustomers(Pageable pageable) {
                Page<CustomerProfileDTO> customers = customerService.getAllCustomers(pageable);
                return ResponseEntity.ok(customers);
        }

        @CrossOrigin(originPatterns = "*")
@DeleteMapping("/customer/{id}")
        public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {
                customerService.deleteCustomer(id);
                return ResponseEntity.ok("Customer deleted successfully");
        }

        // @GetMapping("/technician")
        // public ResponseEntity<Page<TechnicianProfileDTO>> getAllTechnicians(Pageable
        // pageable) {
        // Page<TechnicianProfileDTO> technicians =
        // technicianService.getAllTechnicians(pageable);
        // return ResponseEntity.ok(technicians);
        // }

        @CrossOrigin(originPatterns = "*")
@DeleteMapping("/technician/{id}")
        public ResponseEntity<String> deleteTechnician(@PathVariable Long id) {
                technicianService.deleteTechnician(id);
                return ResponseEntity.ok("Technician deleted successfully");
        }

        @CrossOrigin(originPatterns = "*")
@DeleteMapping("/service/{id}")
        public ResponseEntity<String> deleteService(@PathVariable Long id) {
                serviceService.deleteService(id);
                return ResponseEntity.ok("Service deleted successfully");
        }

        @GetMapping("/operators")
        public ResponseEntity<Page<OperatorProfileDTO>> getAllOperators(Pageable pageable) {
                Page<OperatorProfileDTO> operators = operatorService.getAllOperators(pageable);
                return ResponseEntity.ok(operators);
        }

        @CrossOrigin(originPatterns = "*")
@DeleteMapping("/operator/{id}")
        public ResponseEntity<String> deleteOperator(@PathVariable Long id) {
                operatorService.deleteOperator(id);
                return ResponseEntity.ok("Operator deleted successfully");
        }

        @GetMapping("/bookings")
        public ResponseEntity<Page<BookingDetailDTO>> getFilteredBookings(
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String service,
                        @RequestParam(required = false) BookingStatus status,
                        Pageable pageable) {

                Page<BookingDetailDTO> bookings = bookingService.getFilteredBookings(name, service, status, pageable);
                return ResponseEntity.ok(bookings);
        }

        @GetMapping("/disputes")
        public ResponseEntity<Page<DisputeDetailDTO>> getFilteredDisputes(
                        @RequestParam(required = false) String customerName,
                        @RequestParam(required = false) String technicianName,
                        @RequestParam(required = false) DisputeStatus status,
                        Pageable pageable) {

                Page<DisputeDetailDTO> disputes = disputeService.getFilteredDisputes(customerName, technicianName,
                                status, pageable);
                return ResponseEntity.ok(disputes);
        }

        @GetMapping("/customers")
        public ResponseEntity<Page<CustomerDetailDTO>> getFilteredCustomers(
                        @RequestParam(required = false) String name,
                        Pageable pageable) {

                Page<CustomerDetailDTO> customers = customerService.getFilteredCustomers(name, pageable);
                return ResponseEntity.ok(customers);
        }

        @GetMapping("/technicians")
        public ResponseEntity<Page<TechnicianDetailDTO>> getFilteredTechnicians(
                        @RequestParam(required = false) String name,
                        Pageable pageable) {

                Page<TechnicianDetailDTO> technicians = technicianService.getFilteredTechnicians(name, pageable);
                return ResponseEntity.ok(technicians);
        }

        @PostMapping("/suspend/{userId}")
        public ResponseEntity<String> suspendUser(@PathVariable Long userId) {
                String message = userService.suspendUser(userId);
                return ResponseEntity.ok(message);
        }

        @PostMapping("/delete/{userId}")
        public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
                String message = userService.deleteUser(userId);
                return ResponseEntity.ok(message);
        }

        @PostMapping("/question")
        public ResponseEntity<String> createQuestion(@Valid @RequestBody QuestionRequest request) {
                return ResponseEntity.status(201).body(questionService.createQuestion(request));
        }

        @PostMapping("/service-icons")
        public ResponseEntity<Void> addIconsToServices(@RequestParam Map<String, MultipartFile> serviceIcons) {
                Map<Long, MultipartFile> iconsMap = serviceIcons.entrySet().stream()
                                .collect(Collectors.toMap(
                                                entry -> Long.parseLong(entry.getKey()),
                                                Map.Entry::getValue));
                serviceService.addIconsToServices(iconsMap);
                isUpdated = true;
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/category-icons")
        public ResponseEntity<Void> addIconsToCategories(@RequestParam Map<String, MultipartFile> categoryIcons) {
                Map<Long, MultipartFile> iconsMap = categoryIcons.entrySet().stream()
                                .collect(Collectors.toMap(
                                                entry -> Long.parseLong(entry.getKey()),
                                                Map.Entry::getValue));
                serviceCategoryService.addIconsToCategories(iconsMap);
                isUpdated = true;
                return ResponseEntity.noContent().build();
        }

        @PostMapping(value = "/import", consumes = { "multipart/form-data" })
        public String importServices(
                        @RequestParam("excelFile") MultipartFile excelFile,
                        @RequestParam(value = "iconFiles", required = false) MultipartFile[] iconFiles) throws IOException {

                // Read Excel data
                List<ServiceImportDTO> servicesToImport = readExcelFile(excelFile.getInputStream());

                // Create a map of icon filenames to their stored paths (optional)
                Map<String, String> iconMap = new HashMap<>();
                if (iconFiles != null && iconFiles.length > 0) {
                        for (MultipartFile iconFile : iconFiles) {
                                if (iconFile != null && !iconFile.isEmpty()) {
                                        String storedPath = fileStorageService.storeFile(iconFile);
                                        iconMap.put(iconFile.getOriginalFilename(), storedPath);
                                }
                        }
                }

                // Pass both the services and icon map to the service layer
                serviceService.importServices(servicesToImport, iconMap);
                isUpdated = true;
                return "Services imported successfully";
        }

        private List<ServiceImportDTO> readExcelFile(InputStream inputStream) throws IOException {
                List<ServiceImportDTO> services = new ArrayList<>();
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);

                for (Row row : sheet) {
                        if (row.getRowNum() == 0)
                                continue; // Skip header row

                        if (row.getCell(0) == null)
                                continue; // Skip completely empty rows

                        ServiceImportDTO dto = new ServiceImportDTO();

                        // Column indices per requirement:
                        // 0 level, 1 EN name, 2 EN desc, 3 AM name, 4 AM desc,
                        // 5 OM name, 6 OM desc, 7 TI name, 8 TI desc,
                        // 9 SO name, 10 SO desc, 11 icon
                        if (row.getCell(0) != null) {
                                dto.setLevel((int) row.getCell(0).getNumericCellValue());
                        } else {
                                dto.setLevel(0);
                        }

                        dto.setNameEnglish(getCellString(row, 1));
                        dto.setDescriptionEnglish(getCellString(row, 2));
                        dto.setNameAmharic(getCellString(row, 3));
                        dto.setDescriptionAmharic(getCellString(row, 4));
                        dto.setNameOromo(getCellString(row, 5));
                        dto.setDescriptionOromo(getCellString(row, 6));
                        dto.setNameTigrinya(getCellString(row, 7));
                        dto.setDescriptionTigrinya(getCellString(row, 8));
                        dto.setNameSomali(getCellString(row, 9));
                        dto.setDescriptionSomali(getCellString(row, 10));
                        dto.setIconFileName(getCellString(row, 11));

                        services.add(dto);
                }

                workbook.close();
                return services;
        }

        // Safe string fetch converting any cell to trimmed string
        private String getCellString(Row row, int idx) {
                if (row == null)
                        return "";
                if (row.getCell(idx) == null)
                        return "";
                switch (row.getCell(idx).getCellType()) {
                        case STRING:
                                return row.getCell(idx).getStringCellValue().trim();
                        case NUMERIC:
                                return String.valueOf(row.getCell(idx).getNumericCellValue());
                        case BOOLEAN:
                                return Boolean.toString(row.getCell(idx).getBooleanCellValue());
                        case FORMULA:
                                return row.getCell(idx).getCellFormula();
                        default:
                                return "";
                }
        }

        @PostMapping("/reset-db")
        public ResponseEntity<String> resetDatabase() {
                try {
                        // Get all table names from PostgreSQL's public schema
                        List<String> tableNames = jdbcTemplate.queryForList(
                                        "SELECT tablename FROM pg_tables WHERE schemaname = 'public'", String.class);

                        // Disable foreign key constraints temporarily
                        jdbcTemplate.execute("SET session_replication_role = 'replica'");

                        // Drop all tables
                        for (String tableName : tableNames) {
                                jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName + " CASCADE");
                        }

                        // Re-enable foreign key constraints
                        jdbcTemplate.execute("SET session_replication_role = 'origin'");

                        // Recreate schema using Hibernate
                        recreateSchema();

                        return ResponseEntity.ok("Database reset successfully!");
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error resetting database: " + e.getMessage());
                }
        }

        private void recreateSchema() {
                // Create Hibernate StandardServiceRegistry
                StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                                .applySettings(entityManagerFactory.getProperties()) // Apply JPA properties
                                .build();

                // Add entity classes manually if MetadataSources is empty
                MetadataSources metadataSources = new MetadataSources(standardRegistry);

                // Automatically scan for all entity classes
                entityManagerFactory.getMetamodel().getEntities().forEach(entityType -> {
                        metadataSources.addAnnotatedClass(entityType.getJavaType());
                });

                // Build Metadata
                Metadata metadata = metadataSources.buildMetadata();

                // Drop and recreate schema
                new SchemaExport()
                                .setHaltOnError(true)
                                .setFormat(true)
                                .create(EnumSet.of(TargetType.DATABASE), metadata);
        }

}
