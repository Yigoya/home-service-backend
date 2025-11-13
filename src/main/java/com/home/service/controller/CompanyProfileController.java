package com.home.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.ProfileService;
import com.home.service.dto.CompanyProfileDto;
import com.home.service.dto.CompanyProfileRequest;
import com.home.service.dto.CompanySearchDto;
import com.home.service.dto.CompanyDetailDto;
import com.home.service.dto.ProfileMapper;
import com.home.service.models.CompanyProfile;
import com.home.service.services.FileStorageService;

import java.util.Map;

@CrossOrigin(originPatterns = "*")
@RestController
@RequestMapping("/profiles/company")
@RequiredArgsConstructor
public class CompanyProfileController {
    
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;
    private final FileStorageService fileStorageService;

    @CrossOrigin(originPatterns = "*")
    @PostMapping
    // @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Map<String, Object>> createCompanyProfile(@ModelAttribute CompanyProfileRequest request) {
        CompanyProfileDto dto = new CompanyProfileDto();
        dto.setCompanyId(request.getUserId());
        dto.setName(request.getName());
        dto.setLocation(request.getLocation());
        dto.setDescription(request.getDescription());
        dto.setIndustry(request.getIndustry());
        dto.setSize(request.getSize());
        dto.setFounded(request.getFounded());
        dto.setWebsite(request.getWebsite());
        dto.setEmail(request.getEmail());
        dto.setPhone(request.getPhone());
        dto.setRating(request.getRating());
        dto.setTotalReviews(request.getTotalReviews());
        dto.setOpenJobs(request.getOpenJobs());
        dto.setTotalHires(request.getTotalHires());
        dto.setBenefits(request.getBenefits());
        
        // Additional company registration fields
        dto.setCompanyType(request.getCompanyType());
        dto.setCountry(request.getCountry());
        dto.setCity(request.getCity());
        dto.setBusinessLicense(request.getBusinessLicense());
        dto.setLinkedinPage(request.getLinkedinPage());
        
        // Contact person information
        dto.setContactPersonFullName(request.getContactPersonFullName());
        dto.setContactPersonJobTitle(request.getContactPersonJobTitle());
        dto.setContactPersonWorkEmail(request.getContactPersonWorkEmail());
        dto.setContactPersonWorkPhone(request.getContactPersonWorkPhone());
        
        // Handle logo file upload
        if (request.getLogo() != null && !request.getLogo().isEmpty()) {
            String logoFileName = fileStorageService.storeFile(request.getLogo());
            dto.setLogo(logoFileName);
        }
        
        // Handle cover image file upload
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            String coverImageFileName = fileStorageService.storeFile(request.getCoverImage());
            dto.setCoverImage(coverImageFileName);
        }
        
        // Handle company documents file upload
        if (request.getCompanyDocuments() != null && !request.getCompanyDocuments().isEmpty()) {
            String companyDocumentsFileName = fileStorageService.storeFile(request.getCompanyDocuments());
            dto.setCompanyDocuments(companyDocumentsFileName);
        }
        
        Map<String, Object> response = profileService.createCompanyProfileWithAuth(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @CrossOrigin(originPatterns = "*")
    @PutMapping
    // @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<CompanyProfileDto> updateCompanyProfile(@ModelAttribute CompanyProfileRequest request) {
        CompanyProfileDto dto = new CompanyProfileDto();
        dto.setCompanyId(request.getUserId());
        dto.setName(request.getName());
        dto.setLocation(request.getLocation());
        dto.setDescription(request.getDescription());
        dto.setIndustry(request.getIndustry());
        dto.setSize(request.getSize());
        dto.setFounded(request.getFounded());
        dto.setWebsite(request.getWebsite());
        dto.setEmail(request.getEmail());
        dto.setPhone(request.getPhone());
        dto.setRating(request.getRating());
        dto.setTotalReviews(request.getTotalReviews());
        dto.setOpenJobs(request.getOpenJobs());
        dto.setTotalHires(request.getTotalHires());
        dto.setBenefits(request.getBenefits());
        
        // Additional company registration fields
        dto.setCompanyType(request.getCompanyType());
        dto.setCountry(request.getCountry());
        dto.setCity(request.getCity());
        dto.setBusinessLicense(request.getBusinessLicense());
        dto.setLinkedinPage(request.getLinkedinPage());
        
        // Contact person information
        dto.setContactPersonFullName(request.getContactPersonFullName());
        dto.setContactPersonJobTitle(request.getContactPersonJobTitle());
        dto.setContactPersonWorkEmail(request.getContactPersonWorkEmail());
        dto.setContactPersonWorkPhone(request.getContactPersonWorkPhone());
        
        // Handle logo file upload
        if (request.getLogo() != null && !request.getLogo().isEmpty()) {
            String logoFileName = fileStorageService.storeFile(request.getLogo());
            dto.setLogo(logoFileName);
        }
        
        // Handle cover image file upload
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            String coverImageFileName = fileStorageService.storeFile(request.getCoverImage());
            dto.setCoverImage(coverImageFileName);
        }
        
        // Handle company documents file upload
        if (request.getCompanyDocuments() != null && !request.getCompanyDocuments().isEmpty()) {
            String companyDocumentsFileName = fileStorageService.storeFile(request.getCompanyDocuments());
            dto.setCompanyDocuments(companyDocumentsFileName);
        }
        
        CompanyProfile profile = profileService.createOrUpdateCompanyProfile(dto);
        return ResponseEntity.ok(profileMapper.toCompanyProfileDto(profile));
    }
    @CrossOrigin(originPatterns = "*")
    @GetMapping("/{userId}")
    public ResponseEntity<CompanyProfileDto> getCompanyProfileById(@PathVariable Long userId) {
        CompanyProfile profile = profileService.getCompanyProfileByUserId(userId);
        return ResponseEntity.ok(profileMapper.toCompanyProfileDto(profile));
    }

    @CrossOrigin(originPatterns = "*")
    @GetMapping("/search")
    public ResponseEntity<Page<CompanySearchDto>> searchCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String location,
            Pageable pageable) {
        Page<CompanySearchDto> companies = profileService.searchCompanies(name, industry, location, pageable);
        return ResponseEntity.ok(companies);
    }

    @CrossOrigin(originPatterns = "*")
    @GetMapping("/detail/{companyId}")
    public ResponseEntity<CompanyDetailDto> getCompanyDetail(@PathVariable Long companyId) {
        CompanyDetailDto companyDetail = profileService.getCompanyDetail(companyId);
        return ResponseEntity.ok(companyDetail);
    }
}