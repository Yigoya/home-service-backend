package com.home.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.TenderService;
import com.home.service.dto.ServiceRequest;
import com.home.service.dto.TenderDTO;
import com.home.service.dto.TenderRequest;
import com.home.service.dto.TenderSearchCriteria;
import com.home.service.models.Tender;
import com.home.service.models.enums.TenderStatus;


import java.io.IOException;
import java.time.LocalDateTime;
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

    @CrossOrigin(originPatterns = "*")
    @PutMapping
    public ResponseEntity<String> updateTender(@RequestPart TenderRequest tenderRequest,
            @RequestPart(required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(tenderService.updateTender(tenderRequest, file));
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/{id}/status")
    public ResponseEntity<TenderDTO> changeStatus(@PathVariable Long id, @RequestParam TenderStatus status) {
        return ResponseEntity.ok(tenderService.changeTenderStatus(id, status));
    }

    // Add a service to an agency
    @PostMapping("/add-service")
    public ResponseEntity<String> addService(@ModelAttribute ServiceRequest service) {
        return ResponseEntity.ok(tenderService.addService(service));
    }

    @CrossOrigin(originPatterns = "*")
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
            @RequestParam(required = false, name = "serviceIds") List<Long> serviceIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (serviceIds != null && !serviceIds.isEmpty()) {
            return ResponseEntity.ok(tenderService.getTendersByServices(serviceIds, page, size));
        }
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

    @PostMapping("/search")
    public Page<TenderDTO> searchTenders(@RequestBody TenderSearchCriteria criteria) {
        // adapt single serviceId to list for backward compatibility
        List<Long> serviceIds = null;
        if (criteria.getServiceId() != null) {
            serviceIds = List.of(criteria.getServiceId());
        }
        return tenderService.searchTenders(
                criteria.getKeyword(),
                criteria.getStatus(),
                criteria.getLocation(),
                serviceIds,
                criteria.getIsFree(),
                criteria.getDatePosted(),
                criteria.getClosingDate(),
                criteria.getPage(),
                criteria.getSize());
    }

    @GetMapping("/tenders/search")
    public ResponseEntity<Page<Tender>> advancedSearch(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false, name = "categoryIds") String categoryIdsRaw,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) TenderStatus status,
            @RequestParam(required = false) Boolean isFree,
            Pageable pageable) {
    List<Long> categoryIds = null;
    if (categoryIdsRaw != null && !categoryIdsRaw.isBlank()) {
        categoryIds = java.util.Arrays.stream(categoryIdsRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::valueOf)
            .toList();
    }
    return ResponseEntity.ok(tenderService.advancedSearch(
                keyword, categoryIds, location, dateFrom, dateTo, status, isFree, pageable));
    }

    @GetMapping("/service")
    public ResponseEntity<Page<TenderDTO>> getTendersByServices(@RequestParam List<Long> serviceIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tenderService.getTendersByServices(serviceIds, page, size));
    }

    @GetMapping("/tenders/archive")
    public ResponseEntity<List<Tender>> getArchiveTenders(@RequestParam Long customerId) {
        return ResponseEntity.ok(tenderService.getArchiveTenders(customerId));
    }
}
