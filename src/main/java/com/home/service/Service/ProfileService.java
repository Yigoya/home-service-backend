package com.home.service.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.home.service.config.exceptions.ResourceNotFoundException;
import com.home.service.dto.CompanyDetailDto;
import com.home.service.dto.CompanyProfileDto;
import com.home.service.dto.CompanySearchDto;
import com.home.service.dto.CreateJobSeekerProfileRequest;
import com.home.service.dto.EducationDto;
import com.home.service.dto.ExperienceDto;
import com.home.service.dto.JobSeekerProfileDto;
import com.home.service.dto.ProfileMapper;
import com.home.service.dto.UpdateJobSeekerProfileRequest;
import com.home.service.models.CompanyProfile;
import com.home.service.models.Education;
import com.home.service.models.Experience;
import com.home.service.models.JobSeekerProfile;
import com.home.service.models.User;
import com.home.service.repositories.CompanyProfileRepository;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.ServiceTranslationRepository;
// import com.home.service.repositories.ServiceRepository;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.Business;
import com.home.service.repositories.EducationRepository;
import com.home.service.repositories.ExperienceRepository;
import com.home.service.repositories.JobApplicationRepository;
import com.home.service.repositories.JobRepository;
import com.home.service.repositories.JobSeekerProfileRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.FileStorageService;
import com.home.service.config.JwtUtil;
import com.home.service.dto.UserResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final ExperienceRepository experienceRepository;
    private final EducationRepository educationRepository;
    private final FileStorageService fileStorageService;
    private final ProfileMapper profileMapper;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JwtUtil jwtUtil;
    private final BusinessRepository businessRepository;
    private final ServiceTranslationRepository serviceTranslationRepository;
    // private final ServiceRepository serviceRepository;

    // ============== COMPANY PROFILE LOGIC ==============

    @Transactional
    public Map<String, Object> createCompanyProfileWithAuth(CompanyProfileDto dto) {
        CompanyProfile profile = createOrUpdateCompanyProfile(dto);
        // Soft-sync to Business and attach matching Service by industry
        try { syncBusinessAndService(profile); } catch (Exception ignore) {}
        User user = profile.getUser();
        
        // Generate new token
        String token = jwtUtil.generateToken(user.getEmail());
        
        // Create user response
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getRole().name(),
            user.getStatus().name(),
            user.getProfileImage(),
            user.getPreferredLanguage()
        );
        
        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userResponse);
        response.put("companyId", profile.getId());
        response.put("message", "Company profile created successfully");
        
        return response;
    }

    @Transactional
    public CompanyProfile createOrUpdateCompanyProfile(CompanyProfileDto dto) {
        User user = userRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getCompanyId()));
        System.out.println("Creating or updating company profile for user: " + user.getEmail());
        CompanyProfile profile = companyProfileRepository.findById(user.getId()).orElse(new CompanyProfile());
        System.out.println(dto.getName() + " - " + dto.getLocation() + " iiid- " + dto.getIndustry());
        profile.setUser(user);
        profile.setName(dto.getName());
        profile.setCompanyName(dto.getName()); // Set both name and companyName to the same value
        profile.setLocation(dto.getLocation());
        profile.setDescription(dto.getDescription());
        profile.setIndustry(dto.getIndustry());
        profile.setSize(dto.getSize());
        profile.setFounded(dto.getFounded());
        profile.setWebsite(dto.getWebsite());
        profile.setEmail(dto.getEmail());
        profile.setPhone(dto.getPhone());
        profile.setRating(dto.getRating());
        profile.setTotalReviews(dto.getTotalReviews());
        profile.setOpenJobs(dto.getOpenJobs());
        profile.setTotalHires(dto.getTotalHires());
        profile.setBenefits(dto.getBenefits());
        
        // Additional company registration fields
        profile.setCompanyType(dto.getCompanyType());
        profile.setCountry(dto.getCountry());
        profile.setCity(dto.getCity());
        profile.setBusinessLicense(dto.getBusinessLicense());
        profile.setLinkedinPage(dto.getLinkedinPage());
        
        // Contact person information
        profile.setContactPersonFullName(dto.getContactPersonFullName());
        profile.setContactPersonJobTitle(dto.getContactPersonJobTitle());
        profile.setContactPersonWorkEmail(dto.getContactPersonWorkEmail());
        profile.setContactPersonWorkPhone(dto.getContactPersonWorkPhone());
        
        if (dto.getLogo() != null) {
            profile.setLogo(dto.getLogo());
        }
        
        if (dto.getCoverImage() != null) {
            profile.setCoverImage(dto.getCoverImage());
        }
        
        if (dto.getCompanyDocuments() != null) {
            profile.setCompanyDocuments(dto.getCompanyDocuments());
        }
        
    CompanyProfile saved = companyProfileRepository.save(profile);
    // Soft-sync to Business and attach matching Service by industry
    try { syncBusinessAndService(saved); } catch (Exception ignore) {}
    return saved;
    }
 
    @Transactional(readOnly = true)
    public CompanyProfile getCompanyProfileByUserId(Long userId) {
        return companyProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile", "user id", userId));
    }

    @Transactional(readOnly = true)
    public Page<CompanySearchDto> searchCompanies(String name, String industry, String location, Pageable pageable) {
        Page<CompanyProfile> companyProfiles = companyProfileRepository.searchCompanies(name, industry, location, pageable);
        LocalDate currentDate = LocalDate.now();
        
        return companyProfiles.map(profile -> {
            // Calculate open jobs count
            Integer openJobsCount = jobRepository.countActiveJobsByCompanyId(profile.getId(), currentDate);
            
            return new CompanySearchDto(
                profile.getId(),
                profile.getName(),
                profile.getLogo(),
                profile.getIndustry(),
                profile.getLocation(),
                profile.getSize() != null ? profile.getSize() : "Not specified",
                profile.getRating() != null ? profile.getRating() : 0.0,
                profile.getTotalReviews() != null ? profile.getTotalReviews() : 0,
                openJobsCount != null ? openJobsCount : 0,
                profile.getDescription(),
                profile.getFounded()
            );
        });
    }

    @Transactional(readOnly = true)
    public CompanyDetailDto getCompanyDetail(Long companyId) {
        CompanyProfile profile = companyProfileRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile", "id", companyId));
        return profileMapper.toCompanyDetailDto(profile, jobRepository, jobApplicationRepository);
    }

    // ============== JOB SEEKER PROFILE LOGIC ==============

    @Transactional
    public Map<String, Object> createJobSeekerProfileWithAuth(CreateJobSeekerProfileRequest request, MultipartFile resumeFile) {
        JobSeekerProfile profile = createJobSeekerProfile(request, resumeFile);
        User user = profile.getUser();
        
        // Generate new token
        String token = jwtUtil.generateToken(user.getEmail());
        
        // Create user response
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getRole().name(),
            user.getStatus().name(),
            user.getProfileImage(),
            user.getPreferredLanguage()
        );
        
        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userResponse);
        response.put("jobSeekerId", profile.getId());
        response.put("message", "Job seeker profile created successfully");
        
        return response;
    }

    @Transactional
    public JobSeekerProfile createJobSeekerProfile(CreateJobSeekerProfileRequest request, MultipartFile resumeFile) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        
        // Check if profile already exists
        if (jobSeekerProfileRepository.findById(user.getId()).isPresent()) {
            throw new IllegalStateException("Profile already exists for user: " + request.getUserId());
        }
        
        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setUser(user);
        profile.setHeadline(request.getHeadline());
        profile.setSummary(request.getSummary());
        profile.setSkills(request.getSkills());
        
        // Handle resume upload if provided
        if (resumeFile != null && !resumeFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(resumeFile);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            profile.setResumeUrl(fileDownloadUri);
        }
        
        return jobSeekerProfileRepository.save(profile);
    }

    @Transactional
    public JobSeekerProfile updateJobSeekerProfile(UpdateJobSeekerProfileRequest request, Long profileId, MultipartFile resumeFile, MultipartFile profileImageFile) {
        JobSeekerProfile profile = jobSeekerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeekerProfile", "id", profileId));
        
        // Update JobSeekerProfile fields
        if (request.getHeadline() != null) {
            profile.setHeadline(request.getHeadline());
        }
        if (request.getSummary() != null) {
            profile.setSummary(request.getSummary());
        }
        if (request.getSkills() != null) {
            profile.setSkills(request.getSkills());
        }
        
        // Handle resume upload if provided
        if (resumeFile != null && !resumeFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(resumeFile);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            profile.setResumeUrl(fileDownloadUri);
        }
        
        // Update User fields
        User user = profile.getUser();
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPreferredLanguage() != null) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }
        
        // Handle profile image upload if provided
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(profileImageFile);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            user.setProfileImage(fileDownloadUri);
        }
        
        // Save user first, then profile
        userRepository.save(user);
        return jobSeekerProfileRepository.save(profile);
    }

    @Transactional
    public JobSeekerProfile updateResume(MultipartFile file, Long id) {
        JobSeekerProfile profile = findSeekerProfileById(id);
        String fileName = fileStorageService.storeFile(file);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/") // This path needs to be configured to be publicly accessible
                .path(fileName)
                .toUriString();
        profile.setResumeUrl(fileDownloadUri);
        return jobSeekerProfileRepository.save(profile);
    }

    // ============== EXPERIENCE LOGIC ==============

    @Transactional
    public Experience addExperience(ExperienceDto dto, Long userId) {
        JobSeekerProfile profile = findSeekerProfileById(userId);
        Experience experience = profileMapper.toExperienceEntity(dto);
        experience.setJobSeekerProfile(profile);
        return experienceRepository.save(experience);
    }

    @Transactional
    public Experience updateExperience(Long expId, ExperienceDto dto, Long userId) {
        JobSeekerProfile profile = findSeekerProfileById(userId);
        Experience experience = experienceRepository.findById(expId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", expId));
        
        verifyOwnership(profile, experience.getJobSeekerProfile().getId());

        experience.setJobTitle(dto.getJobTitle());
        experience.setCompanyName(dto.getCompanyName());
        experience.setLocation(dto.getLocation());
        experience.setStartDate(dto.getStartDate());
        experience.setEndDate(dto.getEndDate());
        experience.setDescription(dto.getDescription());
        return experienceRepository.save(experience);
    }

    @Transactional
    public void deleteExperience(Long expId, Long userId) {
        JobSeekerProfile profile = findSeekerProfileById(userId);
        Experience experience = experienceRepository.findById(expId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", expId));
        verifyOwnership(profile, experience.getJobSeekerProfile().getId());
        experienceRepository.delete(experience);
    }

    // ============== EDUCATION LOGIC ==============

    @Transactional
    public Education addEducation(EducationDto dto, Long userId) {
        JobSeekerProfile profile = findSeekerProfileById(userId);
        Education education = profileMapper.toEducationEntity(dto);
        education.setJobSeekerProfile(profile);
        return educationRepository.save(education);
    }

    @Transactional
    public Education updateEducation(Long eduId, EducationDto dto, Long userId) {
        JobSeekerProfile profile = findSeekerProfileById(userId);
        Education education = educationRepository.findById(eduId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", "id", eduId));

        verifyOwnership(profile, education.getJobSeekerProfile().getId());

        education.setInstitutionName(dto.getInstitutionName());
        education.setDegree(dto.getDegree());
        education.setFieldOfStudy(dto.getFieldOfStudy());
        education.setStartDate(dto.getStartDate());
        education.setEndDate(dto.getEndDate());
        return educationRepository.save(education);
    }

    @Transactional
    public void deleteEducation(Long eduId, Long userId) {
        JobSeekerProfile profile = findSeekerProfileById(userId);
        Education education = educationRepository.findById(eduId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", "id", eduId));
        verifyOwnership(profile, education.getJobSeekerProfile().getId());
        educationRepository.delete(education);
    }

    // ============== HELPER & SECURITY METHODS ==============

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
    
    @Transactional(readOnly = true)
    public JobSeekerProfile findSeekerProfileById(Long id) {
        return jobSeekerProfileRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("JobSeekerProfile", "id", id));
    }

    private void verifyOwnership(JobSeekerProfile profile, Long ownerId) {
        if (!profile.getId().equals(ownerId)) {
            throw new AccessDeniedException("You do not have permission to modify this resource.");
        }
    }

    // Ensure a Business exists for this CompanyProfile's user and link a Service by industry name when resolvable
    private void syncBusinessAndService(CompanyProfile profile) {
        User user = profile.getUser();
        Business business = businessRepository.findFirstByOwner(user).orElseGet(() -> {
            Business b = new Business();
            b.setOwner(user);
            b.setName(profile.getName());
            b.setEmail(profile.getEmail());
            b.setPhoneNumber(profile.getPhone());
            b.setWebsite(profile.getWebsite());
            b.setDescription(profile.getDescription());
            b.setIndustry(profile.getIndustry());
            return b;
        });

        // Try to resolve a service by industry name (case-insensitive)
        if (profile.getIndustry() != null && !profile.getIndustry().isBlank()) {
            serviceTranslationRepository.findFirstByNameIgnoreCase(profile.getIndustry().trim())
                .map(ServiceTranslation::getService)
                .ifPresent(svc -> {
                    if (business.getServices() == null) {
                        business.setServices(new java.util.HashSet<>());
                    }
                    business.getServices().add(svc);
                });
        }

        businessRepository.save(business);
    }
}