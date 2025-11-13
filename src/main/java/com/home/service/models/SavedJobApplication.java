package com.home.service.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "saved_job_applications", uniqueConstraints = {
    // A company can only save a specific job application once
    @UniqueConstraint(columnNames = {"company_user_id", "job_application_id"})
})
@Data
@NoArgsConstructor
public class SavedJobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The company user who saved the application
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_user_id", nullable = false)
    private User companyUser;

    // The job application that was saved
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_application_id", nullable = false)
    private JobApplication jobApplication;

    @CreationTimestamp
    private Instant savedAt;

    public SavedJobApplication(User companyUser, JobApplication jobApplication) {
        this.companyUser = companyUser;
        this.jobApplication = jobApplication;
    }
}
