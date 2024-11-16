package com.home.service.Service;

import org.springframework.data.jpa.domain.Specification;

import com.home.service.models.Dispute;
import com.home.service.models.enums.DisputeStatus;

public class DisputeSpecification {
    public static Specification<Dispute> hasCustomerName(String name) {
        return (root, query, criteriaBuilder) -> name != null
                ? criteriaBuilder.like(root.get("customer").get("user").get("name"), "%" + name + "%")
                : null;
    }

    public static Specification<Dispute> hasTechnicianName(String name) {
        return (root, query, criteriaBuilder) -> name != null
                ? criteriaBuilder.like(root.get("technician").get("user").get("name"), "%" + name + "%")
                : null;
    }

    public static Specification<Dispute> hasStatus(DisputeStatus status) {
        return (root, query, criteriaBuilder) -> status != null ? criteriaBuilder.equal(root.get("status"), status)
                : null;
    }
}
