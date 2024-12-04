package com.home.service.Service;

import org.springframework.data.jpa.domain.Specification;

import com.home.service.models.Services;
import com.home.service.models.Technician;
import com.home.service.models.TechnicianServicePrice;
import com.home.service.models.TechnicianWeeklySchedule;

import jakarta.persistence.criteria.*;
import java.time.LocalTime;

public class TechnicianSpecificationSchedule {

    public static Specification<Technician> filterTechnicians(
            String name,
            Double minPrice,
            Double maxPrice,
            String city,
            String subcity,
            String wereda,
            Double minLatitude,
            Double maxLatitude,
            Double minLongitude,
            Double maxLongitude,
            Boolean availability,
            LocalTime time,
            String dayOfWeek,
            Long serviceId) {

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Name filter
            if (name != null && !name.isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(criteriaBuilder.lower(root.join("user").get("name")),
                                "%" + name.toLowerCase() + "%"));
            }

            // Location filters
            if (city != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("technicianAddresses").get("city"), city));
            }
            if (subcity != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("technicianAddresses").get("subcity"), subcity));
            }
            if (wereda != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("technicianAddresses").get("wereda"), wereda));
            }
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

            // Availability filter
            if (availability != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("availability"), availability));
            }

            // Schedule filter based on day of the week and single time parameter
            if (time != null && dayOfWeek != null) {
                Join<Technician, TechnicianWeeklySchedule> scheduleJoin = root.join("weeklySchedule", JoinType.LEFT);

                switch (dayOfWeek.toLowerCase()) {
                    case "monday":
                        predicate = criteriaBuilder.and(predicate,
                                criteriaBuilder.or(
                                        criteriaBuilder.isNull(scheduleJoin),

                                        criteriaBuilder.and(
                                                criteriaBuilder.lessThanOrEqualTo(
                                                        scheduleJoin.get("mondayStart"), time),
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                        scheduleJoin.get("mondayEnd"), time))));
                        break;
                    case "tuesday":
                        predicate = criteriaBuilder.and(predicate,
                                criteriaBuilder.or(
                                        criteriaBuilder.isNull(scheduleJoin),

                                        criteriaBuilder.and(
                                                criteriaBuilder.lessThanOrEqualTo(
                                                        scheduleJoin.get("tuesdayStart"), time),
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                        scheduleJoin.get("tuesdayEnd"), time))));
                        break;
                    case "wednesday":
                        predicate = criteriaBuilder.and(predicate,
                                criteriaBuilder.or(
                                        criteriaBuilder.isNull(scheduleJoin),

                                        criteriaBuilder.and(
                                                criteriaBuilder.lessThanOrEqualTo(
                                                        scheduleJoin.get("wednesdayStart"), time),
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                        scheduleJoin.get("wednesdayEnd"), time))));
                        break;
                    case "thursday":
                        predicate = criteriaBuilder.and(predicate,
                                criteriaBuilder.or(
                                        criteriaBuilder.isNull(scheduleJoin),

                                        criteriaBuilder.and(
                                                criteriaBuilder.lessThanOrEqualTo(
                                                        scheduleJoin.get("thursdayStart"), time),
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                        scheduleJoin.get("thursdayEnd"), time))));
                        break;
                    case "friday":
                        predicate = criteriaBuilder.and(predicate,
                                criteriaBuilder.or(
                                        criteriaBuilder.isNull(scheduleJoin),

                                        criteriaBuilder.and(
                                                criteriaBuilder.lessThanOrEqualTo(
                                                        scheduleJoin.get("fridayStart"), time),
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                        scheduleJoin.get("fridayEnd"), time))));
                        break;
                    case "saturday":
                        predicate = criteriaBuilder.and(predicate,
                                criteriaBuilder.or(
                                        criteriaBuilder.isNull(scheduleJoin),

                                        criteriaBuilder.and(
                                                criteriaBuilder.lessThanOrEqualTo(
                                                        scheduleJoin.get("saturdayStart"), time),
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                        scheduleJoin.get("saturdayEnd"), time))));
                        break;
                    case "sunday":
                        predicate = criteriaBuilder.and(predicate,
                                criteriaBuilder.or(
                                        criteriaBuilder.isNull(scheduleJoin),
                                        criteriaBuilder.and(
                                                criteriaBuilder.lessThanOrEqualTo(
                                                        scheduleJoin.get("sundayStart"), time),
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                        scheduleJoin.get("sundayEnd"), time))));
                        break;
                }
            }

            // Service filter
            if (serviceId != null) {
                Join<Technician, Services> serviceJoin = root.join("services", JoinType.INNER);
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(serviceJoin.get("id"), serviceId));
            }

            // Price filter
            if (minPrice != null || maxPrice != null) {
                Join<Technician, TechnicianServicePrice> priceJoin = root.join("servicePrices", JoinType.INNER);

                if (serviceId != null) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.equal(priceJoin.get("service").get("id"), serviceId));
                }
                if (minPrice != null) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.greaterThanOrEqualTo(priceJoin.get("price"), minPrice));
                }
                if (maxPrice != null) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.lessThanOrEqualTo(priceJoin.get("price"), maxPrice));
                }
            }

            return predicate;
        };
    }
}
