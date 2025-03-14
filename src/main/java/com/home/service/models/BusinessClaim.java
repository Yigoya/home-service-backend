package com.home.service.models;

import java.util.List;

import com.home.service.models.enums.ClaimStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "business_claims")
@Getter
@Setter
public class BusinessClaim extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    @ElementCollection
    private List<String> proofDocuments;

    // Getters and Setters
}
