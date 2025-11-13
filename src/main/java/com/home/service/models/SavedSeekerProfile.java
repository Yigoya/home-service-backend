package com.home.service.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "saved_seeker_profiles", uniqueConstraints = {
    // A company can only save a specific seeker once
    @UniqueConstraint(columnNames = {"company_user_id", "job_seeker_profile_id"})
})
@Data
@NoArgsConstructor
public class SavedSeekerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The company user who saved the profile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_user_id", nullable = false)
    private User companyUser;

    // The profile that was saved
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_profile_id", nullable = false)
    private JobSeekerProfile jobSeekerProfile;

    @CreationTimestamp
    private Instant savedAt;

    public SavedSeekerProfile(User companyUser, JobSeekerProfile jobSeekerProfile) {
        this.companyUser = companyUser;
        this.jobSeekerProfile = jobSeekerProfile;
    }
}