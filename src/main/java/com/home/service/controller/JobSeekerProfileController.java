package com.home.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.Service.ProfileService;
import com.home.service.dto.EducationDto;
import com.home.service.dto.ExperienceDto;
import com.home.service.dto.CreateJobSeekerProfileRequest;
import com.home.service.dto.JobSeekerProfileDto;
import com.home.service.dto.UpdateJobSeekerProfileRequest;
import com.home.service.dto.ProfileMapper;
import com.home.service.models.Education;
import com.home.service.models.Experience;
import com.home.service.models.JobSeekerProfile;

import java.util.Map;

@CrossOrigin(originPatterns = "*")
@RestController
@RequestMapping("/profiles/seeker")
@RequiredArgsConstructor
public class JobSeekerProfileController {
    
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    @CrossOrigin(originPatterns = "*")
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Map<String, Object>> createProfile(
            @ModelAttribute CreateJobSeekerProfileRequest request,
            @RequestParam(value = "resume", required = false) MultipartFile resumeFile) {
        Map<String, Object> response = profileService.createJobSeekerProfileWithAuth(request, resumeFile);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Map<String, Long>> updateProfile(
            @PathVariable Long id,
            @ModelAttribute UpdateJobSeekerProfileRequest request,
            @RequestParam(value = "resume", required = false) MultipartFile resumeFile,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImageFile) {
        JobSeekerProfile profile = profileService.updateJobSeekerProfile(request, id, resumeFile, profileImageFile);
        return ResponseEntity.ok(Map.of("jobSeekerId", profile.getId()));
    }

    @CrossOrigin(originPatterns = "*")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<JobSeekerProfileDto> getMyProfile(@PathVariable Long id) {
        JobSeekerProfile profile = profileService.findSeekerProfileById(id);
        return ResponseEntity.ok(profileMapper.toJobSeekerProfileDto(profile));
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/{id}/resume")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file, @PathVariable Long id) {
        JobSeekerProfile profile = profileService.updateResume(file, id);
        return ResponseEntity.ok(Map.of("resumeUrl", profile.getResumeUrl()));
    }

    // --- Experience Endpoints ---
    @CrossOrigin(originPatterns = "*")
    @PostMapping("/{id}/experience")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ExperienceDto> addExperience(@RequestBody ExperienceDto dto, @PathVariable Long id) {
        Experience newExperience = profileService.addExperience(dto, id);
        return new ResponseEntity<>(profileMapper.toExperienceDto(newExperience), HttpStatus.CREATED);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/{id}/experience/{expId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ExperienceDto> updateExperience(@PathVariable Long expId, @RequestBody ExperienceDto dto, @PathVariable Long id) {
        Experience updatedExperience = profileService.updateExperience(expId, dto, id);
        return ResponseEntity.ok(profileMapper.toExperienceDto(updatedExperience));
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/{id}/experience/{expId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long expId, @PathVariable Long id) {
        profileService.deleteExperience(expId, id);
        return ResponseEntity.noContent().build();
    }
    
    // --- Education Endpoints ---
    @CrossOrigin(originPatterns = "*")
    @PostMapping("/{id}/education")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<EducationDto> addEducation(@RequestBody EducationDto dto, @PathVariable Long id) {
        Education newEducation = profileService.addEducation(dto, id);
        return new ResponseEntity<>(profileMapper.toEducationDto(newEducation), HttpStatus.CREATED);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/{id}/education/{eduId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<EducationDto> updateEducation(@PathVariable Long eduId, @RequestBody EducationDto dto, @PathVariable Long id) {
        Education updatedEducation = profileService.updateEducation(eduId, dto, id);
        return ResponseEntity.ok(profileMapper.toEducationDto(updatedEducation));
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("/{id}/education/{eduId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Void> deleteEducation(@PathVariable Long eduId, @PathVariable Long id) {
        profileService.deleteEducation(eduId, id);
        return ResponseEntity.noContent().build();
    }
}