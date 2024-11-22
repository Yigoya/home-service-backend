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
        return (root, query, criteriaBuilder) -> status != null
                ? criteriaBuilder.equal(root.get("status"), status)
                : null;
    }

    public static Specification<Dispute> hasCity(String city) {
        return (root, query, criteriaBuilder) -> city != null
                ? criteriaBuilder.like(root.get("booking").get("serviceLocation").get("city"), "%" + city + "%")
                : null;
    }

    public static Specification<Dispute> hasSubcity(String subcity) {
        return (root, query, criteriaBuilder) -> subcity != null
                ? criteriaBuilder.like(root.get("booking").get("serviceLocation").get("subcity"), "%" + subcity + "%")
                : null;
    }

    public static Specification<Dispute> hasCityAndSubcity(String city, String subcity) {
        return (root, query, criteriaBuilder) -> {
            if (city != null && subcity != null) {
                return criteriaBuilder.and(
                        criteriaBuilder.like(root.get("booking").get("serviceLocation").get("city"), "%" + city + "%"),
                        criteriaBuilder.like(root.get("booking").get("serviceLocation").get("subcity"),
                                "%" + subcity + "%"));
            } else if (city != null) {
                return criteriaBuilder.like(root.get("booking").get("serviceLocation").get("city"), "%" + city + "%");
            } else if (subcity != null) {
                return criteriaBuilder.like(root.get("booking").get("serviceLocation").get("subcity"),
                        "%" + subcity + "%");
            }
            return null;
        };
    }
}
