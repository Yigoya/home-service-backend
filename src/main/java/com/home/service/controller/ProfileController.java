package com.home.service.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.BookingService;
import com.home.service.Service.CustomerService;
import com.home.service.Service.TechnicianService;
import com.home.service.Service.UserService;
import com.home.service.dto.TechnicianPortfolioDTO;
import com.home.service.dto.AddressDTO;
import com.home.service.dto.ChangeContactRequest;
import com.home.service.dto.CustomerProfileDTO;
import com.home.service.dto.PreferredLanguageRequest;
import com.home.service.dto.ProfileUpdateDTO;
import com.home.service.dto.TechnicianProfileDTO;
import com.home.service.dto.TechnicianWeeklyScheduleResponse;
import com.home.service.dto.WeeklyScheduleRequest;
import com.home.service.services.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/technician/{id}")
    public ResponseEntity<TechnicianProfileDTO> getTechnicianProfile(@PathVariable Long id) {
        TechnicianProfileDTO profile = technicianService.getTechnicianProfile(id);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{technicianId}/active")
    public ResponseEntity<Boolean> isTechnicianActive(@PathVariable Long technicianId) {
        try {
            boolean isActive = technicianService.isTechnicianActive(technicianId);
            return ResponseEntity.ok(isActive);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(false);
        }
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<CustomerProfileDTO> getCustomerProfile(@PathVariable Long id) {
        CustomerProfileDTO profile = customerService.getCustomerProfile(id);
        return ResponseEntity.ok(profile);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/technician/{id}")
    public ResponseEntity<String> updateTechnicianProfile(
            @PathVariable Long id,
            @RequestBody ProfileUpdateDTO updateDTO) {
        technicianService.updateTechnicianProfile(id, updateDTO);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/customer/{id}")
    public ResponseEntity<String> updateCustomerProfile(
            @PathVariable Long id,
            @RequestBody ProfileUpdateDTO updateDTO) {
        customerService.updateCustomerProfile(id, updateDTO);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PostMapping("/technician/{technicianId}/weekly-schedule")
    public ResponseEntity<TechnicianWeeklyScheduleResponse> setWeeklySchedule(
            @PathVariable Long technicianId,
            @RequestBody WeeklyScheduleRequest request) {

        TechnicianWeeklyScheduleResponse schedule = technicianService.setWeeklySchedule(
                technicianId,
                request.getMondayStart(), request.getMondayEnd(),
                request.getTuesdayStart(), request.getTuesdayEnd(),
                request.getWednesdayStart(), request.getWednesdayEnd(),
                request.getThursdayStart(), request.getThursdayEnd(),
                request.getFridayStart(), request.getFridayEnd(),
                request.getSaturdayStart(), request.getSaturdayEnd(),
                request.getSundayStart(), request.getSundayEnd());

        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/customer/{id}/addresses")
    public ResponseEntity<List<AddressDTO>> getCustomerAddresses(@PathVariable Long id) {
        List<AddressDTO> addresses = customerService.getCustomerAddresses(id);
        return ResponseEntity.ok(addresses);
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/customer/{customerId}/address/{addressId}")
    public ResponseEntity<String> deleteCustomerAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId) {
        customerService.deleteCustomerAddress(customerId, addressId);
        return ResponseEntity.ok("Address deleted successfully");
    }

    // @CrossOrigin(originPatterns = "*")
@DeleteMapping("/technician/{technicianId}/address/{addressId}")
    // public ResponseEntity<String> deleteTechnicianAddress(
    // @PathVariable Long technicianId,
    // @PathVariable Long addressId) {
    // technicianService.deleteTechnicianAddress(technicianId, addressId);
    // return ResponseEntity.ok("Address deleted successfully");
    // }

    @GetMapping("/technician/{id}/addresses")
    public ResponseEntity<List<AddressDTO>> getTechnicianAddresses(@PathVariable Long id) {
        List<AddressDTO> addresses = technicianService.getTechnicianAddresses(id);
        return ResponseEntity.ok(addresses);
    }

    @PostMapping("/uploadProfileImage/{userId}")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @PathVariable Long userId,
            @RequestBody MultipartFile file) {
        // Check if the file is empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "File is empty"));
        }

        // Store the file
        String fileName = fileStorageService.storeFile(file);

        // Update the user's profile image URL
        userService.updateProfileImage(userId, fileName);

        // Return the profile image URL
        Map<String, String> response = new HashMap<>();
        response.put("profileImage", fileName);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/toggle-availability/{id}")
    public ResponseEntity<String> toggleAvailability(@PathVariable Long id) {
        boolean updatedStatus = technicianService.toggleAvailability(id);
        return ResponseEntity
                .ok("Technician availability updated to: " + (updatedStatus ? "Available" : "Unavailable"));
    }

    @GetMapping("/technician/schedule/{technicianId}")
    public ResponseEntity<List<Map<String, Object>>> getTechnicianSchedule(
            @PathVariable Long technicianId) {

        List<Map<String, Object>> schedule = bookingService.getTechnicianSchedule(technicianId);
        return ResponseEntity.ok(schedule);
    }

    // Technician portfolio endpoints
    @GetMapping("/technician/{technicianId}/portfolio")
    public ResponseEntity<List<TechnicianPortfolioDTO>> getTechnicianPortfolio(@PathVariable Long technicianId) {
        List<TechnicianPortfolioDTO> portfolio = technicianService.getPortfolio(technicianId);
        return ResponseEntity.ok(portfolio);
    }

    @PostMapping(value = "/technician/{technicianId}/portfolio", consumes = { "multipart/form-data" })
    public ResponseEntity<String> addPortfolioItem(
        @PathVariable Long technicianId,
        @ModelAttribute com.home.service.dto.TechnicianPortfolioRequest request) {

    MultipartFile effectiveBefore = (request.getBeforeImage() != null)
        ? request.getBeforeImage()
        : request.getBefore();
    MultipartFile effectiveAfter = (request.getAfterImage() != null)
        ? request.getAfterImage()
        : request.getAfter();

    technicianService.addPortfolioItem(technicianId, request.getDescription(), effectiveBefore, effectiveAfter);
    return ResponseEntity.ok("Portfolio item added successfully");
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/technician/{technicianId}/portfolio/{portfolioId}")
    public ResponseEntity<String> deletePortfolioItem(
            @PathVariable Long technicianId,
            @PathVariable Long portfolioId) {
        technicianService.deletePortfolioItem(technicianId, portfolioId);
        return ResponseEntity.ok("Portfolio item deleted successfully");
    }

    @PatchMapping("/{userId}/preferred-language")
    public ResponseEntity<String> updatePreferredLanguage(
            @PathVariable Long userId,
            @RequestBody PreferredLanguageRequest request) {
        userService.updatePreferredLanguage(userId, request.getPreferredLanguage());
        return ResponseEntity.ok("Preferred language updated successfully.");
    }

    @PostMapping("/change-email")
    public ResponseEntity<String> changeEmail(@Valid @RequestBody ChangeContactRequest request) {
        userService.initiateChangeEmail(request);
        return ResponseEntity.ok("Email change request initiated. Check your email for verification instructions.");
    }

    @PostMapping("/change-phone")
    public ResponseEntity<String> changePhone(@Valid @RequestBody ChangeContactRequest request) {
        userService.initiateChangePhoneNumber(request);
        return ResponseEntity
                .ok("Phone number change request initiated. Check your phone for verification instructions.");
    }

    @GetMapping("/verify-email-change")
    public ResponseEntity<String> verifyEmailChange(@RequestParam("token") String token) {
        userService.verifyEmailChange(token);
        return ResponseEntity.ok("Email change verified successfully.");
    }

}
