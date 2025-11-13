package com.home.service.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "saved_jobs", uniqueConstraints = {
    // A user can only save a specific job once
    @UniqueConstraint(columnNames = {"job_seeker_id", "job_id"})
})
@Data
@NoArgsConstructor
public class SavedJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private User jobSeeker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @CreationTimestamp
    private Instant savedAt;

    public SavedJob(User jobSeeker, Job job) {
        this.jobSeeker = jobSeeker;
        this.job = job;
    }
}