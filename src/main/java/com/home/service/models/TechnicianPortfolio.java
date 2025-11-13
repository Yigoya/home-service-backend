package com.home.service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianPortfolio extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "technician_id")
    private Technician technician;

    // Optional short description of the work
    private String description;

    // Before and after image URLs (file paths)
    private String beforeImage;
    private String afterImage;
}
