package com.home.service.Service;

import org.springframework.data.jpa.domain.Specification;

import com.home.service.models.Services;
import com.home.service.models.Technician;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;

public class TechnicianSpecification {

    public static Specification<Technician> getTechnicianFilter(
            Long serviceId, String name, Double minPrice, Double maxPrice, String locationQuery,
            String subcity, String wereda, Double minLatitude, Double maxLatitude,
            Double minLongitude, Double maxLongitude) {

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Service filter
            if (serviceId != null) {
                Join<Technician, Services> serviceJoin = root.join("services", JoinType.INNER);
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(serviceJoin.get("id"), serviceId));
            }

            // Name filter
            if (name != null && !name.isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(criteriaBuilder.lower(root.join("user").get("name")),
                                "%" + name.toLowerCase() + "%"));
            }

            // Location filters
            if (locationQuery != null) {
                Predicate cityPredicate = criteriaBuilder.equal(root.get("technicianAddresses").get("city"),
                        locationQuery);
                Predicate subcityPredicate = criteriaBuilder.equal(root.get("technicianAddresses").get("subcity"),
                        locationQuery);
                Predicate weredaPredicate = criteriaBuilder.equal(root.get("technicianAddresses").get("wereda"),
                        locationQuery);

                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.or(cityPredicate, subcityPredicate, weredaPredicate));

            }
            // if (subcity != null) {
            // predicate = criteriaBuilder.and(predicate,
            // criteriaBuilder.equal(root.get("technicianAddresses").get("subcity"),
            // subcity));
            // }
            // if (wereda != null) {
            // predicate = criteriaBuilder.and(predicate,
            // criteriaBuilder.equal(root.get("technicianAddresses").get("wereda"),
            // wereda));
            // }
            if (minLatitude != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("latitude"), minLatitude));
            }
            if (maxLatitude != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("latitude"), maxLatitude));
            }
            if (minLongitude != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("longitude"), minLongitude));
            }
            if (maxLongitude != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("longitude"), maxLongitude));
            }

            // // Availability filter
            // if (availability != null) {
            // predicate = criteriaBuilder.and(predicate,
            // criteriaBuilder.equal(root.get("availability"), availability));
            // }

            // // Filter by price range
            // if (minPrice != null || maxPrice != null) {
            // Join<Technician, TechnicianServicePrice> servicePriceJoin =
            // root.join("technicianServicePrices",
            // JoinType.LEFT);
            // if (minPrice != null) {
            // predicates.add(cb.greaterThanOrEqualTo(servicePriceJoin.get("price"),
            // minPrice));
            // }
            // if (maxPrice != null) {
            // predicates.add(cb.lessThanOrEqualTo(servicePriceJoin.get("price"),
            // maxPrice));
            // }
            // }

            return predicate;
        };
    }
}