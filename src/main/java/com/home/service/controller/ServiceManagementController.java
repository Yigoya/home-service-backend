package com.home.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.BookingService;
import com.home.service.Service.ContactUsService;
import com.home.service.Service.DisputeService;
import com.home.service.Service.ReviewService;
import com.home.service.Service.ServiceCategoryService;
import com.home.service.Service.ServiceService;
import com.home.service.Service.TechnicianService;
import com.home.service.dto.SetServicePriceDTO;
import com.home.service.dto.SingleBookingResponseDTO;
import com.home.service.dto.TechnicianDTO;
import com.home.service.dto.records.SingleTechnician;
import com.home.service.models.Booking;
import com.home.service.models.ContactUs;
import com.home.service.models.ServiceCategory;
import com.home.service.models.Services;
import com.home.service.models.Technician;
import com.home.service.dto.ContactUsRequest;
import com.home.service.dto.DisputeDTO;
import com.home.service.dto.DisputeRequest;
import com.home.service.dto.ReviewDTO;
import com.home.service.dto.ServiceCategoryWithServicesDTO;
import com.home.service.dto.ServiceDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

@RestController
@RequestMapping
public class ServiceManagementController {
    @Autowired
    private ServiceService serviceService;

    @Autowired
    private ServiceCategoryService serviceCategoryService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private ContactUsService contactUsService;

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/home")
    public ResponseEntity<Map<String, Object>> getDataForHome() {
        List<ServiceDTO> service = serviceService.getAllServices();
        List<ServiceCategory> serviceCategory = serviceCategoryService.getAllServiceCategories();
        List<TechnicianDTO> topFiveTechnician = technicianService.getTopFiveTechniciansByRating();
        List<ReviewDTO> topFiveReviews = reviewService.getTop5ReviewsByRating();

        Map<String, Object> response = Map.of("services", service, "serviceCategories", serviceCategory,
                "topFiveTechnicians", topFiveTechnician, "topFiveReviews", topFiveReviews);

        return ResponseEntity.ok(response);

    }

    // Technician Endpoints
    @GetMapping("/technicians")
    public List<Technician> getAllTechnicians() {
        return technicianService.getAllTechnicians();
    }

    @PostMapping("/set-service-price")
    public ResponseEntity<String> setServicePrice(@RequestBody SetServicePriceDTO setServicePriceDTO) {
        technicianService.setServicePrice(setServicePriceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Service price set successfully");
    }

    @GetMapping("/technicians/{id}")
    public SingleTechnician getTechnicianById(@PathVariable Long id) {
        return technicianService.getTechnicianById(id);
    }

    // Service Endpoints
    @GetMapping("/services")
    public List<ServiceDTO> getAllServices() {
        return serviceService.getAllServices();
    }

    @GetMapping("/services/{id}")
    public Map<String, Object> getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id);
    }

    @PutMapping("/services/{id}")
    public Services updateService(@PathVariable Long id, @RequestBody Services service) {
        service.setId(id);
        return serviceService.saveService(service);
    }

    @DeleteMapping("/services/{id}")
    public void deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
    }

    // Service Category Endpoints
    @GetMapping("/service-categories")
    public List<ServiceCategory> getAllServiceCategories() {
        return serviceCategoryService.getAllServiceCategories();
    }

    @GetMapping("/service-categorie/{id}")
    public ResponseEntity<ServiceCategoryWithServicesDTO> getServiceCategoryById(@PathVariable Long id) {
        Optional<ServiceCategoryWithServicesDTO> serviceCategory = serviceCategoryService
                .getServiceCategoryWithServicesById(id);
        return serviceCategory.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/service-categories")
    public ServiceCategory createServiceCategory(@RequestBody ServiceCategory serviceCategory) {
        return serviceCategoryService.saveServiceCategory(serviceCategory);
    }

    @PutMapping("/service-categories/{id}")
    public ServiceCategory updateServiceCategory(@PathVariable Long id, @RequestBody ServiceCategory serviceCategory) {
        serviceCategory.setId(id);
        return serviceCategoryService.saveServiceCategory(serviceCategory);
    }

    @DeleteMapping("/service-categories/{id}")
    public void deleteServiceCategory(@PathVariable Long id) {
        serviceCategoryService.deleteServiceCategory(id);
    }

    // Booking Endpoints
    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/bookings/{id}")
    public SingleBookingResponseDTO getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @PostMapping("/bookings")
    public Booking createBooking(@RequestBody Booking booking) {
        return bookingService.saveBooking(booking);
    }

    @PutMapping("/bookings/{id}")
    public Booking updateBooking(@PathVariable Long id, @RequestBody Booking booking) {
        booking.setId(id);
        return bookingService.saveBooking(booking);
    }

    @DeleteMapping("/bookings/{id}")
    public void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }

    // Get all services for a technician
    @GetMapping("/{technicianId}/services")
    public Set<Services> getServicesForTechnician(@PathVariable Long technicianId) {
        return technicianService.getServicesForTechnician(technicianId);
    }

    // Add a service to a technician
    @PostMapping("/{technicianId}/services/{serviceId}")
    public Technician addServiceToTechnician(@PathVariable Long technicianId, @PathVariable Long serviceId) {
        return technicianService.addServiceToTechnician(technicianId, serviceId);
    }

    // Remove a service from a technician
    @DeleteMapping("/{technicianId}/services/{serviceId}")
    public Technician removeServiceFromTechnician(@PathVariable Long technicianId, @PathVariable Long serviceId) {
        return technicianService.removeServiceFromTechnician(technicianId, serviceId);
    }

    @PostMapping("/contact-us")
    public ResponseEntity<ContactUs> submitContactUs(@RequestBody ContactUsRequest contactUsRequest) {

        ContactUs contactUs = contactUsService.submitContactUs(contactUsRequest);
        return ResponseEntity.status(201).body(contactUs);
    }

    @PostMapping("/dispute")
    public ResponseEntity<String> submitDispute(@RequestBody DisputeRequest disputeRequest) {

        disputeService.submitDispute(disputeRequest);
        return ResponseEntity.status(201).body("Dispute submitted successfully");
    }

    @GetMapping("/disputes/customer/{customerId}")
    public ResponseEntity<List<DisputeDTO>> getDisputesByCustomerId(@PathVariable Long customerId) {
        List<DisputeDTO> disputes = disputeService.getDisputesByCustomerId(customerId);
        if (disputes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(disputes);
    }
}
