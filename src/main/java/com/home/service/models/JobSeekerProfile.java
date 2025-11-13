package com.home.service.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "job_seeker_profiles")
@Data
@NoArgsConstructor
public class JobSeekerProfile {

    @Id
    private Long id; // Same ID as the User

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String headline;

    @Column(length = 5000)
    private String summary;

    private String resumeUrl; // URL to the stored resume file

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_seeker_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill")
    private Set<String> skills;

    @OneToMany(mappedBy = "jobSeekerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experience;

    @OneToMany(mappedBy = "jobSeekerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> education;
}