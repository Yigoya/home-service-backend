package com.home.service.dto;


import org.springframework.stereotype.Component;

import com.home.service.models.CompanyProfile;
import com.home.service.models.Education;
import com.home.service.models.Experience;
import com.home.service.models.Job;
import com.home.service.models.JobSeekerProfile;
import com.home.service.models.enums.ApplicationStatus;
import com.home.service.repositories.JobApplicationRepository;
import com.home.service.repositories.JobRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.home.service.models.enums.EthiopianLanguage;

@Component
public class ProfileMapper {

    // Company Mappers
    public CompanyProfileDto toCompanyProfileDto(CompanyProfile profile) {
        if (profile == null) return null;
        CompanyProfileDto dto = new CompanyProfileDto();
        dto.setCompanyId(profile.getUser().getId());
        dto.setName(profile.getName());
        dto.setLocation(profile.getLocation());
        dto.setDescription(profile.getDescription());
        dto.setLogo(profile.getLogo());
        dto.setCoverImage(profile.getCoverImage());
        dto.setIndustry(profile.getIndustry());
        dto.setSize(profile.getSize());
        dto.setFounded(profile.getFounded());
        dto.setWebsite(profile.getWebsite());
        dto.setEmail(profile.getEmail());
        dto.setPhone(profile.getPhone());
        dto.setRating(profile.getRating());
        dto.setTotalReviews(profile.getTotalReviews());
        dto.setOpenJobs(profile.getOpenJobs());
        dto.setTotalHires(profile.getTotalHires());
        dto.setBenefits(profile.getBenefits());
        
        // Additional company registration fields
        dto.setCompanyType(profile.getCompanyType());
        dto.setCountry(profile.getCountry());
        dto.setCity(profile.getCity());
        dto.setBusinessLicense(profile.getBusinessLicense());
        dto.setLinkedinPage(profile.getLinkedinPage());
        dto.setCompanyDocuments(profile.getCompanyDocuments());
        
        // Contact person information
        dto.setContactPersonFullName(profile.getContactPersonFullName());
        dto.setContactPersonJobTitle(profile.getContactPersonJobTitle());
        dto.setContactPersonWorkEmail(profile.getContactPersonWorkEmail());
        dto.setContactPersonWorkPhone(profile.getContactPersonWorkPhone());
        
        return dto;
    }

    public CompanyDetailDto toCompanyDetailDto(CompanyProfile profile, JobRepository jobRepository, JobApplicationRepository jobApplicationRepository) {
        if (profile == null) return null;
        
        LocalDate currentDate = LocalDate.now();
        Long companyId = profile.getId();
        
        // Get real data from repositories
        List<Job> activeJobs = jobRepository.findActiveJobsByCompanyId(companyId, currentDate);
        Integer openJobsCount = jobRepository.countActiveJobsByCompanyId(companyId, currentDate);
        Integer totalHires = jobApplicationRepository.countTotalHiredApplicationsByCompany(companyId, ApplicationStatus.HIRED);

        // Build current openings from active jobs
        List<CompanyDetailDto.CurrentOpeningDto> currentOpenings = activeJobs.stream()
            .map(job -> {
                Integer applicantCount = jobApplicationRepository.countApplicationsByJobId(job.getId());
                String postedTime = formatPostedTime(job.getPostedDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                
                // Replace getName() with the correct method or field from Services class, e.g., getServiceName()
                return CompanyDetailDto.CurrentOpeningDto.builder()
                    .id(job.getId())
                    .title(job.getTitle())
                    .department(job.getService() != null ? job.getService().getTranslations().stream()
                                .filter(t -> t.getLang().equals(EthiopianLanguage.ENGLISH))
                                .findFirst()
                                .map(t -> t.getName())
                                .orElse(null) : null)
                    .type(job.getJobType().toString())
                    .posted(postedTime)
                    .applicants(applicantCount != null ? applicantCount : 0)
                    .build();
            })
            .collect(Collectors.toList());
        
        // Build recruiting history for the last 3 years
        List<CompanyDetailDto.RecruitingHistoryDto> recruitingHistory = buildRecruitingHistory(
            companyId, jobApplicationRepository, currentDate.getYear());
        
        // Default benefits and culture (these could be stored in the database in the future)
        List<String> benefits = Arrays.asList(
            "Competitive salary packages", "Health insurance coverage", "Professional development opportunities",
            "Flexible working hours", "Remote work options", "Annual performance bonuses",
            "Team building activities", "Modern office facilities", "Learning and development budget",
            "Career advancement opportunities"
        );
        
        List<String> culture = Arrays.asList(
            "Innovation-driven", "Collaborative", "Growth-oriented", "Inclusive", "Results-focused", "Learning culture"
        );
        
        return CompanyDetailDto.builder()
            .name(profile.getName())
            .logo(profile.getLogo())
            .coverImage("https://images.pexels.com/photos/3184360/pexels-photo-3184360.jpeg?auto=compress&cs=tinysrgb&w=1200&h=400&dpr=2")
            .industry(profile.getIndustry() != null ? profile.getIndustry() : "Information Technology & Software")
            .size(profile.getSize() != null ? profile.getSize() : "50-200 employees")
            .founded(profile.getFounded() != null ? profile.getFounded() : "2018")
            .location(profile.getLocation())
            .website(profile.getWebsite())
            .email(profile.getEmail())
            .phone(profile.getPhone())
            .rating(profile.getRating() != null ? profile.getRating() : 4.5)
            .totalReviews(profile.getTotalReviews() != null ? profile.getTotalReviews() : 0)
            .openJobs(openJobsCount != null ? openJobsCount : 0)
            .totalHires(totalHires != null ? totalHires : 0)
            .description(profile.getDescription())
            .benefits(benefits)
            .culture(culture)
            .recruitingHistory(recruitingHistory)
            .currentOpenings(currentOpenings)
            .build();
    }
    
    private List<CompanyDetailDto.RecruitingHistoryDto> buildRecruitingHistory(Long companyId, JobApplicationRepository jobApplicationRepository, int currentYear) {
        List<CompanyDetailDto.RecruitingHistoryDto> history = new ArrayList<>();
        
        // Get data for the last 3 years
        for (int year = currentYear; year >= currentYear - 2; year--) {
            Integer totalHires = jobApplicationRepository.countHiredApplicationsByCompanyAndYear(companyId, ApplicationStatus.HIRED, year);
            
            if (totalHires != null && totalHires > 0) {
                // Get department-wise hiring data
                List<Object[]> departmentData = jobApplicationRepository.countHiredApplicationsByDepartmentAndYear(companyId, ApplicationStatus.HIRED, year);
                
                List<CompanyDetailDto.DepartmentHireDto> departments = departmentData.stream()
                    .map(data -> CompanyDetailDto.DepartmentHireDto.builder()
                        .name((String) data[0])
                        .hires(((Number) data[1]).intValue())
                        .build())
                    .collect(Collectors.toList());
                
                // Generate some highlights based on the data
                List<String> highlights = generateHighlights(year, totalHires, departments);
                
                CompanyDetailDto.RecruitingHistoryDto yearHistory = CompanyDetailDto.RecruitingHistoryDto.builder()
                    .year(String.valueOf(year))
                    .totalHires(totalHires)
                    .departments(departments)
                    .highlights(highlights)
                    .build();
                
                history.add(yearHistory);
            }
        }
        
        return history;
    }
    
    private List<String> generateHighlights(int year, int totalHires, List<CompanyDetailDto.DepartmentHireDto> departments) {
        List<String> highlights = new ArrayList<>();
        
        if (totalHires > 20) {
            highlights.add("Significant expansion with " + totalHires + " new hires");
        } else if (totalHires > 10) {
            highlights.add("Steady growth with " + totalHires + " new team members");
        }
        
        // Find the department with most hires
        departments.stream()
            .max((d1, d2) -> Integer.compare(d1.getHires(), d2.getHires()))
            .ifPresent(topDept -> {
                if (topDept.getHires() > 5) {
                    highlights.add("Expanded " + topDept.getName() + " team significantly");
                }
            });
        
        // Add some generic highlights based on year
        if (year == LocalDate.now().getYear()) {
            highlights.add("Launched new employee development programs");
        } else if (year == LocalDate.now().getYear() - 1) {
            highlights.add("Achieved high employee retention rate");
        } else {
            highlights.add("Established strong recruitment processes");
        }
        
        return highlights;
    }
    
    private String formatPostedTime(LocalDate postedDate) {
        LocalDate now = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(postedDate, now);
        
        if (daysBetween == 0) {
            return "Today";
        } else if (daysBetween == 1) {
            return "1 day ago";
        } else if (daysBetween < 7) {
            return daysBetween + " days ago";
        } else if (daysBetween < 14) {
            return "1 week ago";
        } else if (daysBetween < 30) {
            long weeks = daysBetween / 7;
            return weeks + " weeks ago";
        } else {
            long months = daysBetween / 30;
            return months + " months ago";
        }
    }

    // Job Seeker Mappers
    public JobSeekerProfileDto toJobSeekerProfileDto(JobSeekerProfile profile) {
        if (profile == null) return null;
        JobSeekerProfileDto dto = new JobSeekerProfileDto();
        
        // Job Seeker Profile fields
        dto.setUserId(profile.getUser().getId());
        dto.setHeadline(profile.getHeadline());
        dto.setSummary(profile.getSummary());
        dto.setResumeUrl(profile.getResumeUrl());
        dto.setSkills(profile.getSkills());
        
        // User fields
        if (profile.getUser() != null) {
            dto.setName(profile.getUser().getName());
            dto.setPhoneNumber(profile.getUser().getPhoneNumber());
            dto.setPhone(profile.getUser().getPhoneNumber()); // alias
            dto.setEmail(profile.getUser().getEmail());
            dto.setRole(profile.getUser().getRole());
            dto.setProfileImage(profile.getUser().getProfileImage());
            dto.setStatus(profile.getUser().getStatus());
            dto.setPreferredLanguage(profile.getUser().getPreferredLanguage());
            
            // Split name into firstName and lastName
            if (profile.getUser().getName() != null) {
                String[] nameParts = profile.getUser().getName().trim().split("\\s+", 2);
                dto.setFirstName(nameParts[0]);
                dto.setLastName(nameParts.length > 1 ? nameParts[1] : "");
            }
        }
        
        // Additional profile fields (aliases and computed fields)
        dto.setTitle(profile.getHeadline()); // alias for headline
        dto.setBio(profile.getSummary()); // alias for summary
        
        // Map experience and education lists
        if (profile.getExperience() != null) {
            dto.setExperience(profile.getExperience().stream()
                .map(this::toExperienceDto)
                .collect(java.util.stream.Collectors.toList()));
        }
        
        if (profile.getEducation() != null) {
            dto.setEducation(profile.getEducation().stream()
                .map(this::toEducationDto)
                .collect(java.util.stream.Collectors.toList()));
        }
        
        // TODO: Add location, linkedin, github fields to JobSeekerProfile model if needed
        // For now, these will be null unless added to the model
        dto.setLocation(null); // Could be derived from user address or profile location
        dto.setLinkedin(null); // Would need to be added to JobSeekerProfile model
        dto.setGithub(null); // Would need to be added to JobSeekerProfile model
        
        return dto;
    }

    // Experience Mappers
    public ExperienceDto toExperienceDto(Experience experience) {
        if (experience == null) return null;
        ExperienceDto dto = new ExperienceDto();
        dto.setId(experience.getId());
        dto.setUserId(experience.getJobSeekerProfile().getUser().getId());
        dto.setJobTitle(experience.getJobTitle());
        dto.setCompanyName(experience.getCompanyName());
        dto.setLocation(experience.getLocation());
        dto.setStartDate(experience.getStartDate());
        dto.setEndDate(experience.getEndDate());
        dto.setDescription(experience.getDescription());
        return dto;
    }

    public Experience toExperienceEntity(ExperienceDto dto) {
        if (dto == null) return null;
        Experience entity = new Experience();
        entity.setId(dto.getId()); // Used for updates
        entity.setJobTitle(dto.getJobTitle());
        entity.setCompanyName(dto.getCompanyName());
        entity.setLocation(dto.getLocation());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    // Education Mappers
    public EducationDto toEducationDto(Education education) {
        if (education == null) return null;
        EducationDto dto = new EducationDto();
        dto.setId(education.getId());
        dto.setInstitutionName(education.getInstitutionName());
        dto.setDegree(education.getDegree());
        dto.setFieldOfStudy(education.getFieldOfStudy());
        dto.setStartDate(education.getStartDate());
        dto.setEndDate(education.getEndDate());
        return dto;
    }
    
    public Education toEducationEntity(EducationDto dto) {
        if (dto == null) return null;
        Education entity = new Education();
        entity.setId(dto.getId()); // Used for updates
        entity.setInstitutionName(dto.getInstitutionName());
        entity.setDegree(dto.getDegree());
        entity.setFieldOfStudy(dto.getFieldOfStudy());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        return entity;
    }
}