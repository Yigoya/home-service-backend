package com.home.service.Service;

import org.springframework.data.jpa.domain.Specification;

import com.home.service.models.Booking;
import com.home.service.models.enums.BookingStatus;

public class BookingSpecification {

    public static Specification<Booking> hasCustomerName(String name) {
        return (root, query, criteriaBuilder) -> name != null
                ? criteriaBuilder.like(root.get("customer").get("user").get("name"), "%" + name + "%")
                : null;
    }

    public static Specification<Booking> hasService(String service) {
        return (root, query, criteriaBuilder) -> service != null
                ? criteriaBuilder.equal(root.get("service").get("name"), service)
                : null;
    }

    public static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, criteriaBuilder) -> status != null
                ? criteriaBuilder.equal(root.get("status"), status)
                : null;
    }

    public static Specification<Booking> hasTechnicianName(String name) {
        return (root, query, criteriaBuilder) -> name != null
                ? criteriaBuilder.like(root.get("technician").get("user").get("name"), "%" + name + "%")
                : null;
    }

    public static Specification<Booking> hasCity(String city) {
        return (root, query, criteriaBuilder) -> city != null
                ? criteriaBuilder.like(root.get("serviceLocation").get("city"), "%" + city + "%")
                : null;
    }

    public static Specification<Booking> hasSubcity(String subcity) {
        return (root, query, criteriaBuilder) -> subcity != null
                ? criteriaBuilder.like(root.get("serviceLocation").get("subcity"), "%" + subcity + "%")
                : null;
    }

    public static Specification<Booking> hasCityAndSubcity(String city, String subcity) {
        return (root, query, criteriaBuilder) -> {
            if (city != null && subcity != null) {
                return criteriaBuilder.and(
                        criteriaBuilder.like(root.get("serviceLocation").get("city"), "%" + city + "%"),
                        criteriaBuilder.like(root.get("serviceLocation").get("subcity"), "%" + subcity + "%"));
            } else if (city != null) {
                return criteriaBuilder.like(root.get("serviceLocation").get("city"), "%" + city + "%");
            } else if (subcity != null) {
                return criteriaBuilder.like(root.get("serviceLocation").get("subcity"), "%" + subcity + "%");
            }
            return null;
        };
    }

    public static Specification<Booking> orderByCreatedAtDesc() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return criteriaBuilder.conjunction();
        };
    }
}
