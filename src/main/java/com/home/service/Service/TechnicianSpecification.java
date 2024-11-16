package com.home.service.Service;

import org.springframework.data.jpa.domain.Specification;
import com.home.service.models.Technician;
import com.home.service.models.TechnicianServicePrice;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.JoinType;

import java.util.ArrayList;
import java.util.List;

public class TechnicianSpecification {

    public static Specification<Technician> getTechnicianFilter(
            String name, Double minPrice, Double maxPrice, String city,
            String subcity, String wereda, Double minLatitude, Double maxLatitude,
            Double minLongitude, Double maxLongitude) {

        return (Root<Technician> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by technician name
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.join("user").get("name")), "%" + name.toLowerCase() + "%"));
            }

            // Filter by city, subcity, and wereda
            if (city != null) {
                predicates.add(cb.equal(root.join("technicianAddress").get("city"), city));
            }
            if (subcity != null) {
                predicates.add(cb.equal(root.join("technicianAddress").get("subcity"), subcity));
            }
            if (wereda != null) {
                predicates.add(cb.equal(root.join("technicianAddress").get("wereda"), wereda));
            }

            // Filter by location (latitude and longitude)
            if (minLatitude != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.join("technicianAddress").get("latitude"), minLatitude));
            }
            if (maxLatitude != null) {
                predicates.add(cb.lessThanOrEqualTo(root.join("technicianAddress").get("latitude"), maxLatitude));
            }
            if (minLongitude != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.join("technicianAddress").get("longitude"), minLongitude));
            }
            if (maxLongitude != null) {
                predicates.add(cb.lessThanOrEqualTo(root.join("technicianAddress").get("longitude"), maxLongitude));
            }

            // Filter by price range
            if (minPrice != null || maxPrice != null) {
                Join<Technician, TechnicianServicePrice> servicePriceJoin = root.join("technicianServicePrices",
                        JoinType.LEFT);
                if (minPrice != null) {
                    predicates.add(cb.greaterThanOrEqualTo(servicePriceJoin.get("price"), minPrice));
                }
                if (maxPrice != null) {
                    predicates.add(cb.lessThanOrEqualTo(servicePriceJoin.get("price"), maxPrice));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
