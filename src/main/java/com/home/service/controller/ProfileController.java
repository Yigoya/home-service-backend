package com.home.service.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.CustomerService;
import com.home.service.Service.TechnicianService;
import com.home.service.Service.UserService;
import com.home.service.dto.CustomerProfileDTO;
import com.home.service.dto.ProfileUpdateDTO;
import com.home.service.dto.TechnicianProfileDTO;
import com.home.service.dto.TechnicianWeeklyScheduleResponse;
import com.home.service.dto.WeeklyScheduleRequest;
import com.home.service.services.FileStorageService;

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

    @GetMapping("/technician/{id}")
    public ResponseEntity<TechnicianProfileDTO> getTechnicianProfile(@PathVariable Long id) {
        TechnicianProfileDTO profile = technicianService.getTechnicianProfile(id);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<CustomerProfileDTO> getCustomerProfile(@PathVariable Long id) {
        CustomerProfileDTO profile = customerService.getCustomerProfile(id);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/technician/{id}")
    public ResponseEntity<String> updateTechnicianProfile(
            @PathVariable Long id,
            @RequestBody ProfileUpdateDTO updateDTO) {
        technicianService.updateTechnicianProfile(id, updateDTO);
        return ResponseEntity.ok("Profile updated successfully");
    }

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

    @PutMapping("/toggle-availability/{id}")
    public ResponseEntity<String> toggleAvailability(@PathVariable Long id) {
        boolean updatedStatus = technicianService.toggleAvailability(id);
        return ResponseEntity
                .ok("Technician availability updated to: " + (updatedStatus ? "Available" : "Unavailable"));
    }
}
