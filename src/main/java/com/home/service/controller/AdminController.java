package com.home.service.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.home.service.dto.ServiceIconRequest;
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
import com.home.service.models.enums.BookingStatus;
import com.home.service.models.enums.DisputeStatus;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.Service.OperatorService;
import com.home.service.Service.PaymentProofService;
import com.home.service.Service.QuestionService;
import com.home.service.Service.ServiceCategoryService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import com.home.service.models.Dispute;
import com.home.service.models.QuestionRequest;
import com.home.service.models.Technician;
import com.home.service.models.TechnicianProofResponse;

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

        @DeleteMapping("/technician/{id}")
        public ResponseEntity<String> deleteTechnician(@PathVariable Long id) {
                technicianService.deleteTechnician(id);
                return ResponseEntity.ok("Technician deleted successfully");
        }

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

        @GetMapping("/services")
        public ResponseEntity<List<ServiceCategoryWithServicesDTO>> getAllServicesCategorized(
                        @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang) {
                List<ServiceCategoryWithServicesDTO> categories = serviceService.getAllServicesCategorized(lang);
                return ResponseEntity.ok(categories);
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

        @PostMapping("/icons")
        public ResponseEntity<Void> addIconsToServices(@RequestParam Map<String, MultipartFile> serviceIcons) {
                Map<Long, MultipartFile> iconsMap = serviceIcons.entrySet().stream()
                                .collect(Collectors.toMap(
                                                entry -> Long.parseLong(entry.getKey()),
                                                Map.Entry::getValue));
                serviceService.addIconsToServices(iconsMap);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/import")
        public String importServices(@RequestParam("file") MultipartFile file) throws IOException {
                List<ServiceImportDTO> servicesToImport = readExcelFile(file.getInputStream());
                serviceService.importServices(servicesToImport);
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
                                break; // Break if the row is empty

                        ServiceImportDTO dto = new ServiceImportDTO();
                        if (row.getCell(0) != null) {
                                dto.setLevel((int) row.getCell(0).getNumericCellValue());
                        }
                        if (row.getCell(1) != null) {
                                dto.setNameEnglish(row.getCell(1).getStringCellValue());
                        }
                        if (row.getCell(2) != null) {
                                dto.setDescriptionEnglish(row.getCell(2).getStringCellValue());
                        }
                        if (row.getCell(3) != null) {
                                dto.setNameAmharic(row.getCell(3).getStringCellValue());
                        }
                        if (row.getCell(4) != null) {
                                dto.setDescriptionAmharic(row.getCell(4).getStringCellValue());
                        }
                        if (row.getCell(5) != null) {
                                dto.setNameOromo(row.getCell(5).getStringCellValue());
                        }
                        if (row.getCell(6) != null) {
                                dto.setDescriptionOromo(row.getCell(6).getStringCellValue());
                        }
                        services.add(dto);
                }

                workbook.close();
                return services;
        }
}
