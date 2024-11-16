package com.home.service.models;

import com.home.service.models.enums.DocumentStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    private String documentType; // e.g., "BANK_TICKET"
    private String fileName; // File name of the uploaded document
    private String fileUrl; // URL or file path of the document

    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.PENDING; // Document status

    // Constructors, getters, and setters...
}
