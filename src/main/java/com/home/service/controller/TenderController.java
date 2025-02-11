package com.home.service.controller;

import org.checkerframework.checker.units.qual.s;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.TenderService;
import com.home.service.dto.AgencyServiceRequest;
import com.home.service.dto.ServiceRequest;
import com.home.service.dto.TenderDTO;
import com.home.service.dto.TenderRequest;
import com.home.service.models.enums.TenderStatus;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/tenders")
public class TenderController {

    @Autowired
    private TenderService tenderService;

    @PostMapping
    public ResponseEntity<String> addTender(@ModelAttribute TenderRequest tenderRequest) throws IOException {
        return ResponseEntity.ok(tenderService.addTender(tenderRequest));
    }

    @PutMapping
    public ResponseEntity<String> updateTender(@RequestPart TenderRequest tenderRequest,
            @RequestPart(required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(tenderService.updateTender(tenderRequest, file));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TenderDTO> changeStatus(@PathVariable Long id, @RequestParam TenderStatus status) {
        return ResponseEntity.ok(tenderService.changeTenderStatus(id, status));
    }

    // Add a service to an agency
    @PostMapping("/add-service")
    public ResponseEntity<String> addService(@ModelAttribute ServiceRequest service) {
        return ResponseEntity.ok(tenderService.addService(service));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTender(@PathVariable Long id) {
        tenderService.deleteTender(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<TenderDTO>> getAllTenders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tenderService.getAllTenders(page, size)); // Example arguments
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<Page<TenderDTO>> getTendersByService(@PathVariable Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tenderService.getTendersByService(serviceId, page, size));
    }

    @GetMapping("/status")
    public ResponseEntity<Page<TenderDTO>> getTendersByStatus(@RequestParam TenderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tenderService.getTendersByStatus(status, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenderDTO> getTenderById(@PathVariable Long id) {
        return ResponseEntity.ok(tenderService.getTenderById(id));
    }

    @GetMapping("/location-service")
    public ResponseEntity<Page<TenderDTO>> getTendersByLocationAndService(@RequestParam String location,
            @RequestParam Long serviceId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tenderService.getTendersByLocationAndService(location, serviceId, page, size));
    }

    @GetMapping("/location")
    public ResponseEntity<Page<TenderDTO>> getTendersByLocation(@RequestParam String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tenderService.getTendersByLocation(location, page, size));
    }
}
