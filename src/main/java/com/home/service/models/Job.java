package com.home.service.models;
// Job.java
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.home.service.models.enums.JobType;
import com.home.service.models.enums.JobStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyProfile company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false) // Changed from category_id
    private Services service; // Changed from category

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    private String jobLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.ACTIVE;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency; // e.g., "ETB", "USD"

    @ElementCollection // For simple lists of strings
    private List<String> responsibilities;

    @ElementCollection
    private List<String> qualifications;

    @ElementCollection
    private List<String> benefits;
    
    @ElementCollection
    private List<String> tags; // e.g., ['React', 'Node.js', 'TypeScript', 'AWS', 'MongoDB']
    
    private String level; // e.g., "Junior", "Mid", "Senior"
    
    private LocalDate applicationDeadline;
    
    private String contactEmail;
    
    private String contactPhone;
    
    @CreationTimestamp
    private Instant postedDate;
}