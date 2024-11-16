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
        return (root, query, criteriaBuilder) -> status != null ? criteriaBuilder.equal(root.get("status"), status)
                : null;
    }
}
