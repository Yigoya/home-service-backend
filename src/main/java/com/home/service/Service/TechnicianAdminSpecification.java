package com.home.service.Service;

import org.springframework.data.jpa.domain.Specification;

import com.home.service.models.Technician;

public class TechnicianAdminSpecification {
    public static Specification<Technician> hasName(String name) {
        return (root, query, criteriaBuilder) -> name != null
                ? criteriaBuilder.like(root.get("user").get("name"), "%" + name + "%")
                : null;
    }
}
