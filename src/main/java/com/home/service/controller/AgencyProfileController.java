package com.home.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.AgencyProfileService;
import com.home.service.Service.BookingService;
import com.home.service.dto.AgencyBookingDTO;
import com.home.service.dto.AgencyBookingRequest;
import com.home.service.dto.AgencyDashboardDTO;
import com.home.service.dto.AgencyProfileDTO;
import com.home.service.dto.AgencyProfileRequest;
import com.home.service.dto.AgencySearchCriteria;
import com.home.service.dto.AgencyServiceRequest;
import com.home.service.models.AgencyBooking;
import com.home.service.models.enums.VerificationStatus;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/agencies")
public class AgencyProfileController {

    @Autowired
    private AgencyProfileService agencyProfileService;

    @Autowired
    private BookingService bookingService;

    @PostMapping()
    public ResponseEntity<AgencyProfileDTO> createAgencyProfile(
            @ModelAttribute AgencyProfileRequest agencyProfileRequest)
            throws IOException {
        AgencyProfileDTO agencyProfile = agencyProfileService.createAgencyProfile(agencyProfileRequest);
        return ResponseEntity.ok(agencyProfile);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AgencyProfileDTO>> getPendingAgencies() {
        List<AgencyProfileDTO> agencies = agencyProfileService.getAllPendingAgencies();
        return ResponseEntity.ok(agencies);
    }

    @PostMapping("/verify/{agencyId}")
    public ResponseEntity<String> verifyAgency(@PathVariable Long agencyId, @RequestParam VerificationStatus status) {
        agencyProfileService.verifyAgency(agencyId, status);
        return ResponseEntity.ok("Status Set Successfully");
    }

    @PostMapping("/booking-request")
    public ResponseEntity<String> createBooking(@RequestBody AgencyBookingRequest agencyBookingRequest) {
        bookingService.createAgencyBooking(agencyBookingRequest.getCustomerId(),
                agencyBookingRequest.getAgencyId(), agencyBookingRequest.getServiceId(), agencyBookingRequest);
        return ResponseEntity.ok("Booking created successfully");
    }

    @GetMapping
    public ResponseEntity<List<AgencyProfileDTO>> getAllAgencies() {
        List<AgencyProfileDTO> agencies = agencyProfileService.getAllAgencies();
        return ResponseEntity.ok(agencies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgencyProfileDTO> getAgencyById(@PathVariable Long id) {
        AgencyProfileDTO agencyProfile = agencyProfileService.getAgencyById(id);
        return ResponseEntity.ok(agencyProfile);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgencyProfileDTO> updateAgencyProfile(@PathVariable Long id,
            @RequestBody AgencyProfileRequest agencyProfileRequest) {
        AgencyProfileDTO agencyProfile = agencyProfileService.updateAgencyProfile(id, agencyProfileRequest);
        return ResponseEntity.ok(agencyProfile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgencyProfile(@PathVariable Long id) {
        agencyProfileService.deleteAgencyProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<AgencyProfileDTO>> searchAgencies(@ModelAttribute AgencySearchCriteria criteria) {
        List<AgencyProfileDTO> agencies = agencyProfileService.searchAgencies(criteria);
        return ResponseEntity.ok(agencies);
    }

    // Add a service to an agency
    @PostMapping("/{id}/services")
    public ResponseEntity<String> addService(@PathVariable Long id, @RequestBody AgencyServiceRequest service) {
        return ResponseEntity.ok(agencyProfileService.addService(id, service));
    }

    @PostMapping("/{agencyId}/service/{serviceId}")
    public ResponseEntity<String> addExistingServiceToAgency(@PathVariable Long agencyId,
            @PathVariable Long serviceId) {
        String response = agencyProfileService.addExistingServiceToAgency(agencyId, serviceId);
        return ResponseEntity.ok(response);
    }

    // Remove a service from an agency
    @DeleteMapping("/{id}/services/{serviceId}")
    public ResponseEntity<Void> removeService(@PathVariable Long id, @PathVariable Long serviceId) {
        agencyProfileService.removeService(id, serviceId);
        return ResponseEntity.noContent().build();
    }

    // Get all bookings for an agency
    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<AgencyBookingDTO>> getAgencyBookings(@PathVariable Long id) {
        List<AgencyBookingDTO> bookings = agencyProfileService.getAgencyBookings(id);
        return ResponseEntity.ok(bookings);
    }

    // Get agency dashboard information
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<AgencyDashboardDTO> getAgencyDashboard(@PathVariable Long id) {
        AgencyDashboardDTO dashboardDTO = agencyProfileService.getAgencyDashboard(id);
        return ResponseEntity.ok(dashboardDTO);
    }

    @GetMapping("/service/{serviceId}")
    public List<AgencyProfileDTO> getAgencyProfilesByServiceId(@PathVariable Long serviceId) {
        return agencyProfileService.findByServiceId(serviceId);
    }
}