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
import com.home.service.models.DistrictService;
import com.home.service.models.Services;
import com.home.service.models.Technician;
import com.home.service.models.enums.EthiopianLanguage;

import jakarta.validation.Valid;

import com.home.service.dto.ContactUsRequest;
import com.home.service.dto.DisputeDTO;
import com.home.service.dto.DisputeRequest;
import com.home.service.dto.District;
import com.home.service.dto.ReviewDTO;
import com.home.service.dto.ServiceCategoryDTO;
import com.home.service.dto.ServiceCategoryWithServicesDTO;
import com.home.service.dto.ServiceDTO;
import com.home.service.dto.ServiceRequest;

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

    @Autowired
    private DistrictService districtService;

    @GetMapping("/home")
    public ResponseEntity<Map<String, Object>> getDataForHome(
            @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang) {
        List<ServiceDTO> service = serviceService.getAllServices(lang);
        List<ServiceCategoryDTO> serviceCategory = serviceCategoryService.getAllServiceCategories(lang);
        List<TechnicianDTO> topFiveTechnician = technicianService.getTopFiveTechniciansByRating(lang);
        List<ReviewDTO> topFiveReviews = reviewService.getTop5ReviewsByRating();
        List<District> districts = districtService.getDistricts(Optional.of(lang.toString().toLowerCase()),
                Optional.empty());
        Map<String, Object> response = Map.of("services", service, "serviceCategories", serviceCategory,
                "topFiveTechnicians", topFiveTechnician, "topFiveReviews", topFiveReviews, "locations", districts);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Test endpoint is working! 🎉 12");
    }

    // Technician Endpoints
    @GetMapping("/technicians")
    public List<Technician> getAllTechnicians() {
        return technicianService.getAllTechnicians();
    }

    @PostMapping("/set-service-price")
    public ResponseEntity<String> setServicePrice(@Valid @RequestBody SetServicePriceDTO setServicePriceDTO) {
        technicianService.setServicePrice(setServicePriceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Service price set successfully");
    }

    @GetMapping("/technicians/{id}")
    public SingleTechnician getTechnicianById(@PathVariable Long id) {
        return technicianService.getTechnicianById(id);
    }

    // Service Endpoints
    @GetMapping("/services")
    public List<ServiceDTO> getAllServices(@RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang) {
        return serviceService.getAllServices(lang);
    }

    @GetMapping("/services/{id}")
    public Map<String, Object> getServiceById(@PathVariable Long id,
            @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang) {
        return serviceService.getServiceById(id, lang);
    }

    @PutMapping("/services/{id}")
    public String updateService(@PathVariable Long id, @Valid @RequestBody ServiceRequest serviceRequest) {
        return serviceService.updateService(id, serviceRequest);
    }

    @DeleteMapping("/services/{id}")
    public void deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
    }

    // Service Category Endpoints
    @GetMapping("/service-categories")
    public List<ServiceCategoryDTO> getAllServiceCategories(
            @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang) {
        return serviceCategoryService.getAllServiceCategories(lang);
    }

    @GetMapping("/service-categories/{id}")
    public ResponseEntity<ServiceCategoryWithServicesDTO> getServiceCategoryById(@PathVariable Long id,
            @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang) {
        Optional<ServiceCategoryWithServicesDTO> serviceCategory = serviceCategoryService
                .getServiceCategoryWithServicesById(id, lang);
        return serviceCategory.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // @PutMapping("/services/{id}/icon")
    // public ResponseEntity<String> updateServiceIcon(@PathVariable Long id,
    // @RequestParam String iconUrl) {
    // serviceService.updateServiceIcon(id, iconUrl);
    // return ResponseEntity.ok("Service icon updated successfully");
    // }

    // @PutMapping("/service-categories/{id}/icon")
    // public ResponseEntity<String> updateServiceCategoryIcon(@PathVariable Long
    // id, @RequestParam String iconUrl) {
    // serviceCategoryService.updateServiceCategoryIcon(id, iconUrl);
    // return ResponseEntity.ok("Service category icon updated successfully");
    // }

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
    public ResponseEntity<ContactUs> submitContactUs(@Valid @RequestBody ContactUsRequest contactUsRequest) {

        ContactUs contactUs = contactUsService.submitContactUs(contactUsRequest);
        return ResponseEntity.status(201).body(contactUs);
    }

    @PostMapping("/dispute")
    public ResponseEntity<String> submitDispute(@Valid @RequestBody DisputeRequest disputeRequest) {

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

    @GetMapping("/districts")
    public List<District> getDistricts(@RequestParam Optional<String> language, @RequestParam Optional<String> query) {
        return districtService.getDistricts(language, query);
    }

    @GetMapping("/services/{serviceId}/subservices")
    public ResponseEntity<List<ServiceDTO>> getServicesByServiceId(@PathVariable Long serviceId,
            @RequestParam(defaultValue = "ENGLISH") EthiopianLanguage lang) {
        List<ServiceDTO> services = serviceService.getServicesByServiceId(serviceId, lang);
        return ResponseEntity.ok(services);
    }

}
