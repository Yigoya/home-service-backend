package com.home.service.Service;

import org.springframework.data.jpa.domain.Specification;

import com.home.service.models.Technician;

public class TechnicianAdminSpecification {

    public static Specification<Technician> hasName(String name) {
        return (root, query, criteriaBuilder) -> name != null
                ? criteriaBuilder.like(root.get("user").get("name"), "%" + name + "%")
                : null;
    }

    public static Specification<Technician> hasCity(String city) {
        return (root, query, criteriaBuilder) -> city != null
                ? criteriaBuilder.like(root.join("technicianAddresses").get("city"), "%" + city + "%")
                : null;
    }

    public static Specification<Technician> hasSubcity(String subcity) {
        return (root, query, criteriaBuilder) -> subcity != null
                ? criteriaBuilder.like(root.join("technicianAddresses").get("subcity"), "%" + subcity + "%")
                : null;
    }

    public static Specification<Technician> hasCityAndSubcity(String city, String subcity) {
        return (root, query, criteriaBuilder) -> {
            if (city != null && subcity != null) {
                return criteriaBuilder.and(
                        criteriaBuilder.like(root.join("technicianAddresses").get("city"), "%" + city + "%"),
                        criteriaBuilder.like(root.join("technicianAddresses").get("subcity"), "%" + subcity + "%"));
            } else if (city != null) {
                return criteriaBuilder.like(root.join("technicianAddresses").get("city"), "%" + city + "%");
            } else if (subcity != null) {
                return criteriaBuilder.like(root.join("technicianAddresses").get("subcity"), "%" + subcity + "%");
            }
            return null;
        };
    }
}
