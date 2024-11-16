package com.home.service.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Technician;
import com.home.service.models.TechnicianDocument;
import com.home.service.models.enums.DocumentStatus;

public interface TechnicianDocumentRepository extends JpaRepository<TechnicianDocument, Long> {
    List<TechnicianDocument> findByTechnicianAndStatus(Technician technician, DocumentStatus status);

    List<TechnicianDocument> findByStatus(DocumentStatus status);
}
