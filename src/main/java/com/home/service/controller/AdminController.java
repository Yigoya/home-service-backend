package com.home.service.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.Service.BookingService;
import com.home.service.Service.CustomerService;
import com.home.service.Service.DisputeService;
import com.home.service.Service.ServiceService;
import com.home.service.Service.TechnicianService;
import com.home.service.dto.CustomerProfileDTO;
import com.home.service.dto.DisputeDTO;
import com.home.service.dto.OperatorProfileDTO;
import com.home.service.dto.TechnicianProfileDTO;
import com.home.service.dto.admin.BookingDetailDTO;
import com.home.service.dto.admin.CustomerDetailDTO;
import com.home.service.dto.admin.DisputeDetailDTO;
import com.home.service.dto.admin.ServiceCategoryWithServicesDTO;
import com.home.service.dto.admin.TechnicianDetailDTO;
import com.home.service.repositories.DisputeRepository;
import com.home.service.repositories.TechnicianRepository;
import com.home.service.services.EmailService;
import com.home.service.models.TechnicianWeeklySchedule;
import com.home.service.models.enums.BookingStatus;
import com.home.service.models.enums.DisputeStatus;
import com.home.service.Service.OperatorService;

import jakarta.persistence.EntityNotFoundException;

import com.home.service.models.Dispute;
import com.home.service.models.Services;
import com.home.service.models.Technician;

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

        @GetMapping("technicians/unverified")
        public ResponseEntity<List<TechnicianProfileDTO>> listUnverifiedTechnicians() {
                List<Technician> unverifiedTechnicians = technicianRepository.findByVerifiedFalse();
                List<TechnicianProfileDTO> technicianDTOs = unverifiedTechnicians.stream()
                                .map(technician -> new TechnicianProfileDTO(
                                                technician.getId(),
                                                technician.getUser().getName(),
                                                technician.getUser().getEmail(),
                                                technician.getBio(),
                                                technician.getRating(),
                                                technician.getCompletedJobs(),
                                                new TechnicianWeeklySchedule()))
                                .collect(Collectors.toList());

                return ResponseEntity.ok(technicianDTOs);
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
        @PutMapping("/dispute/{disputeId}/status")
        public ResponseEntity<String> updateDisputeStatus(
                        @PathVariable Long disputeId,
                        @RequestParam DisputeStatus status) {
                Dispute dispute = disputeRepository.findById(disputeId)
                                .orElseThrow(() -> new EntityNotFoundException("Dispute not found"));

                dispute.setStatus(status);
                disputeRepository.save(dispute);

                return ResponseEntity.ok("Dispute status updated to " + status);
        }

        @PutMapping("/service/{id}")
        public ResponseEntity<Services> updateService(@PathVariable Long id, @RequestBody Services updatedService) {
                Services updated = serviceService.updateService(id, updatedService);
                return ResponseEntity.ok(updated);
        }

        @PostMapping("/services")
        public Services createService(@RequestBody Services service) {
                return serviceService.saveService(service);
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

        @GetMapping("/technician")
        public ResponseEntity<Page<TechnicianProfileDTO>> getAllTechnicians(Pageable pageable) {
                Page<TechnicianProfileDTO> technicians = technicianService.getAllTechnicians(pageable);
                return ResponseEntity.ok(technicians);
        }

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
        public ResponseEntity<List<ServiceCategoryWithServicesDTO>> getAllServicesCategorized() {
                List<ServiceCategoryWithServicesDTO> categories = serviceService.getAllServicesCategorized();
                return ResponseEntity.ok(categories);
        }
}
