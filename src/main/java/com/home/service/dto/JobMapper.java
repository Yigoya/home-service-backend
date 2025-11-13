package com.home.service.dto;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.home.service.models.Job;
import com.home.service.models.JobApplication;
import com.home.service.models.CompanyProfile;
import com.home.service.models.User;
import com.home.service.models.JobSeekerProfile;
import com.home.service.models.Experience;
import com.home.service.models.SavedJob;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.repositories.JobSeekerProfileRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class JobMapper {

    @Autowired
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    public JobSummaryDto toJobSummaryDto(Job job) {
        JobSummaryDto dto = new JobSummaryDto();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setJobLocation(job.getJobLocation());
        dto.setJobType(job.getJobType().name());
        dto.setPostedDate(job.getPostedDate());
        dto.setSalaryMin(job.getSalaryMin());
        dto.setSalaryMax(job.getSalaryMax());
        dto.setSalaryCurrency(job.getSalaryCurrency());
        dto.setLevel(job.getLevel());
        dto.setApplicationDeadline(job.getApplicationDeadline());
        dto.setContactEmail(job.getContactEmail());
        dto.setContactPhone(job.getContactPhone());
        
        if (job.getCompany() != null) {
            dto.setCompanyName(job.getCompany().getName());
            dto.setCompanyLocation(job.getCompany().getLocation());
            dto.setCompanyLogo(job.getCompany().getLogo());
        }
        if (job.getService() != null && job.getService() != null) {
            dto.setCategory(job.getService().getTranslations()
                    .stream()
                    .filter(translation -> translation.getLang().equals(EthiopianLanguage.ENGLISH))
                    .findFirst()
                    .map(translation -> translation.getName())
                    .orElse("Unknown Category"));
        }
        return dto;
    }

    public JobSummaryDto toJobSummaryDto(SavedJob savedJob) {
        JobSummaryDto dto = toJobSummaryDto(savedJob.getJob());
        dto.setSavedDate(savedJob.getSavedAt());
        return dto;
    }

    public JobDetailDto toJobDetailDto(Job job) {
        JobDetailDto dto = new JobDetailDto();
        // Copy properties from summary DTO logic
        JobSummaryDto summaryDto = toJobSummaryDto(job);
        dto.setId(summaryDto.getId());
        dto.setTitle(summaryDto.getTitle());
        dto.setJobLocation(summaryDto.getJobLocation());
        dto.setJobType(summaryDto.getJobType());
        dto.setPostedDate(summaryDto.getPostedDate());
        dto.setSalaryMin(summaryDto.getSalaryMin());
        dto.setSalaryMax(summaryDto.getSalaryMax());
        dto.setSalaryCurrency(summaryDto.getSalaryCurrency());
        dto.setLevel(summaryDto.getLevel());
        dto.setApplicationDeadline(summaryDto.getApplicationDeadline());
        dto.setContactEmail(summaryDto.getContactEmail());
        dto.setContactPhone(summaryDto.getContactPhone());
        dto.setCompanyName(summaryDto.getCompanyName());
        dto.setCompanyLocation(summaryDto.getCompanyLocation());
        dto.setCompanyLogo(summaryDto.getCompanyLogo());
        dto.setCategory(summaryDto.getCategory());

        // Add detailed fields
        dto.setDescription(job.getDescription());
        dto.setResponsibilities(job.getResponsibilities());
        dto.setQualifications(job.getQualifications());
        dto.setBenefits(job.getBenefits());
        dto.setTags(job.getTags());
        
        return dto;
    }

    public JobDetailDto toJobDetailDto(Job job, List<Job> relatedJobs) {
        JobDetailDto dto = toJobDetailDto(job);
        
        // Add company data
        if (job.getCompany() != null) {
            dto.setCompanyData(toCompanyDataDto(job.getCompany()));
        }
        
        // Add related jobs
        if (relatedJobs != null) {
            dto.setRelatedJobs(relatedJobs.stream()
                    .map(this::toRelatedJobDto)
                    .toList());
        }
        
        return dto;
    }

    public CompanyDataDto toCompanyDataDto(CompanyProfile company) {
        CompanyDataDto dto = new CompanyDataDto();
        dto.setName(company.getName());
        dto.setLogo(company.getLogo());
        dto.setDescription(company.getDescription());
        dto.setIndustry(company.getIndustry());
        dto.setSize(company.getSize());
        dto.setFounded(company.getFounded());
        dto.setLocation(company.getLocation());
        dto.setWebsite(company.getWebsite());
        dto.setRating(company.getRating());
        dto.setTotalReviews(company.getTotalReviews());
        dto.setOpenJobs(company.getOpenJobs());
        
        // Set default benefits and culture if not available in the model
        dto.setBenefits(Arrays.asList(
            "Health Insurance", "Flexible Hours", "Remote Work", 
            "Professional Development", "Performance Bonuses"
        ));
        dto.setCulture(Arrays.asList(
            "Innovation-driven", "Collaborative", "Growth-oriented", "Inclusive"
        ));
        
        return dto;
    }

    public RelatedJobDto toRelatedJobDto(Job job) {
        RelatedJobDto dto = new RelatedJobDto();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setLocation(job.getJobLocation());
        dto.setType(job.getJobType().name());
        dto.setLevel(job.getLevel());
        
        if (job.getCompany() != null) {
            dto.setCompany(job.getCompany().getName());
            dto.setLogo(job.getCompany().getLogo());
        }
        
        // Format salary
        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            dto.setSalary(String.format("%,.0f - %,.0f %s", 
                job.getSalaryMin().doubleValue(), 
                job.getSalaryMax().doubleValue(), 
                job.getSalaryCurrency() != null ? job.getSalaryCurrency() : ""));
        }
        
        // Calculate posted time ago
        if (job.getPostedDate() != null) {
            dto.setPosted(formatTimeAgo(job.getPostedDate()));
        }
        
        return dto;
    }

    public CompanyJobDto toCompanyJobDto(Job job, Long applicationCount) {
        CompanyJobDto dto = new CompanyJobDto();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setJobLocation(job.getJobLocation());
        dto.setJobType(job.getJobType().name());
        dto.setSalaryMin(job.getSalaryMin());
        dto.setSalaryMax(job.getSalaryMax());
        dto.setSalaryCurrency(job.getSalaryCurrency());
        dto.setLevel(job.getLevel());
        dto.setPostedDate(job.getPostedDate());
        dto.setApplicationDeadline(job.getApplicationDeadline());
        dto.setContactEmail(job.getContactEmail());
        dto.setContactPhone(job.getContactPhone());
        dto.setTags(job.getTags());
        dto.setResponsibilities(job.getResponsibilities());
        dto.setBenefits(job.getBenefits());
        dto.setApplicationCount(applicationCount);
        
        // Set category from service
        if (job.getService() != null && job.getService().getTranslations() != null) {
            dto.setCategory(job.getService().getTranslations()
                    .stream()
                    .filter(translation -> translation.getLang().equals(EthiopianLanguage.ENGLISH))
                    .findFirst()
                    .map(translation -> translation.getName())
                    .orElse("Unknown Category"));
        }
        
        // Determine job status
        if (job.getApplicationDeadline() != null) {
            if (job.getApplicationDeadline().isBefore(java.time.LocalDate.now())) {
                dto.setStatus("EXPIRED");
            } else {
                dto.setStatus("ACTIVE");
            }
        } else {
            dto.setStatus("ACTIVE");
        }
        
        return dto;
    }

    private String formatTimeAgo(Instant postedDate) {
        Duration duration = Duration.between(postedDate, Instant.now());
        long days = duration.toDays();
        
        if (days == 0) {
            return "Today";
        } else if (days == 1) {
            return "1 day ago";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks == 1 ? "1 week ago" : weeks + " weeks ago";
        } else {
            long months = days / 30;
            return months == 1 ? "1 month ago" : months + " months ago";
        }
    }

    public ApplicationDto toApplicationDto(JobApplication application) {
        ApplicationDto dto = new ApplicationDto();
        
        // New format fields
        dto.setId(application.getId());
        dto.setCandidateName(application.getJobSeeker().getName());
        dto.setEmail(application.getJobSeeker().getEmail());
        dto.setPhone(application.getJobSeeker().getPhoneNumber());
        dto.setJobTitle(application.getJob().getTitle());
        dto.setAppliedDate(formatApplicationDate(application.getApplicationDate()));
        dto.setStatus(formatStatus(application.getStatus().name()));
        dto.setExperience(calculateExperience(application.getJobSeeker()));
        dto.setLocation(extractLocation(application.getJobSeeker()));
        dto.setAvatar(application.getJobSeeker().getProfileImage());
        // Resume fallback: if application lacks resume, pull from JobSeekerProfile
        String resumeUrl = application.getResumeUrl();
        if (resumeUrl == null || resumeUrl.trim().isEmpty()) {
            try {
                Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findById(application.getJobSeeker().getId());
                if (profileOpt.isPresent()) {
                    String profileResume = profileOpt.get().getResumeUrl();
                    if (profileResume != null && !profileResume.trim().isEmpty()) {
                        resumeUrl = profileResume;
                    }
                }
            } catch (Exception e) {
                // swallow and keep resumeUrl null; optionally log if a logger is available
            }
        }
        dto.setResumeUrl(resumeUrl);
        dto.setCoverLetter(application.getCoverLetter());
        dto.setRating(application.getRating());
        
        // Additional fields for getMyApplications
        if (application.getJob().getCompany() != null) {
            dto.setCompanyName(application.getJob().getCompany().getName());
            dto.setCompanyLogo(application.getJob().getCompany().getLogo());
        }
        dto.setJobType(application.getJob().getJobType() != null ? application.getJob().getJobType().name() : null);
        dto.setSalaryRange(formatSalaryRange(application.getJob()));
        
        // Backward compatibility fields
        dto.setJobId(application.getJob().getId());
        dto.setJobSeekerId(application.getJobSeeker().getId());
        dto.setJobSeekerName(application.getJobSeeker().getName());
        dto.setApplicationDate(application.getApplicationDate());
        
        return dto;
    }
    
    private String formatApplicationDate(Instant applicationDate) {
        if (applicationDate == null) return null;
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(java.time.ZoneId.systemDefault())
                .format(applicationDate);
    }
    
    private String formatStatus(String status) {
        if (status == null) return "pending";
        return status.toLowerCase();
    }
    
    private String calculateExperience(User jobSeeker) {
        try {
            Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findById(jobSeeker.getId());
            if (profileOpt.isPresent()) {
                JobSeekerProfile profile = profileOpt.get();
                if (profile.getExperience() != null && !profile.getExperience().isEmpty()) {
                    int totalMonths = 0;
                    LocalDate now = LocalDate.now();
                    
                    for (Experience exp : profile.getExperience()) {
                        if (exp.getStartDate() != null) {
                            LocalDate endDate = exp.getEndDate() != null ? exp.getEndDate() : now;
                            Period period = Period.between(exp.getStartDate(), endDate);
                            totalMonths += period.getYears() * 12 + period.getMonths();
                        }
                    }
                    
                    int years = totalMonths / 12;
                    if (years == 0) {
                        return "Less than 1 year";
                    } else if (years == 1) {
                        return "1 year";
                    } else if (years <= 2) {
                        return "1-2 years";
                    } else if (years <= 5) {
                        return "3-5 years";
                    } else if (years <= 10) {
                        return "5-10 years";
                    } else {
                        return "10+ years";
                    }
                }
            }
        } catch (Exception e) {
            // Log error and return default
        }
        return "Experience not specified";
    }
    
    private String extractLocation(User jobSeeker) {
        try {
            Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findById(jobSeeker.getId());
            if (profileOpt.isPresent()) {
                JobSeekerProfile profile = profileOpt.get();
                // If there's a location field in the profile or experience, use it
                if (profile.getExperience() != null && !profile.getExperience().isEmpty()) {
                    // Get location from most recent experience
                    Optional<String> location = profile.getExperience().stream()
                            .filter(exp -> exp.getLocation() != null && !exp.getLocation().trim().isEmpty())
                            .sorted((e1, e2) -> {
                                LocalDate date1 = e1.getEndDate() != null ? e1.getEndDate() : LocalDate.now();
                                LocalDate date2 = e2.getEndDate() != null ? e2.getEndDate() : LocalDate.now();
                                return date2.compareTo(date1);
                            })
                            .map(Experience::getLocation)
                            .findFirst();
                    
                    if (location.isPresent()) {
                        return location.get();
                    }
                }
            }
        } catch (Exception e) {
            // Log error and return default
        }
        return "Location not specified";
    }
    
    private String formatSalaryRange(Job job) {
        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            String currency = job.getSalaryCurrency() != null ? job.getSalaryCurrency() : "";
            return String.format("%,.0f - %,.0f %s", 
                job.getSalaryMin().doubleValue(), 
                job.getSalaryMax().doubleValue(), 
                currency).trim();
        } else if (job.getSalaryMin() != null) {
            String currency = job.getSalaryCurrency() != null ? job.getSalaryCurrency() : "";
            return String.format("From %,.0f %s", 
                job.getSalaryMin().doubleValue(), 
                currency).trim();
        } else if (job.getSalaryMax() != null) {
            String currency = job.getSalaryCurrency() != null ? job.getSalaryCurrency() : "";
            return String.format("Up to %,.0f %s", 
                job.getSalaryMax().doubleValue(), 
                currency).trim();
        }
        return "Salary not specified";
    }

    public MyApplicationDto toMyApplicationDto(JobApplication application) {
        MyApplicationDto dto = new MyApplicationDto();
        
        dto.setId(application.getId());
        dto.setTitle(application.getJob().getTitle());
        dto.setCompany(application.getJob().getCompany() != null ? 
            application.getJob().getCompany().getName() : "Company not specified");
        dto.setLocation(application.getJob().getJobLocation());
        dto.setSalary(formatSalaryRange(application.getJob()));
        dto.setType(application.getJob().getJobType() != null ? 
            application.getJob().getJobType().name() : "Full-time");
        dto.setLogo(application.getJob().getCompany() != null ? 
            application.getJob().getCompany().getLogo() : 
            "https://images.pexels.com/photos/3182812/pexels-photo-3182812.jpeg?auto=compress&cs=tinysrgb&w=60&h=60&dpr=2");
        dto.setAppliedDate(formatApplicationDate(application.getApplicationDate()));
        dto.setStatus(formatStatus(application.getStatus().name()));
        
        return dto;
    }
}