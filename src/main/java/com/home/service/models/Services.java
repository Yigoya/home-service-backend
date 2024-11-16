package com.home.service.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Services extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    private String name;
    private String description;
    private String estimatedDuration;
    private Double serviceFee;

    @ManyToMany(mappedBy = "services")
    private Set<Question> questions;

    @Transient
    private Long categoryId;

}