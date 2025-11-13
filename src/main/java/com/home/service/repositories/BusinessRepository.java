package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import java.util.List;
import java.util.Optional;

import com.home.service.models.Business;
import com.home.service.models.User;
import com.home.service.models.Services;

public interface BusinessRepository extends JpaRepository<Business, Long>, JpaSpecificationExecutor<Business> {

    @Query("SELECT b FROM Business b WHERE b.location.id = :locationId")
    Page<Business> findByLocationId(Long locationId, Pageable pageable);

    @Query("SELECT b FROM Business b WHERE b.isFeatured = true")
    Page<Business> findByIsFeaturedTrue(Pageable pageable);

    Page<Business> findByOwner(User owner, Pageable pageable);

    Optional<Business> findFirstByOwner(User owner);

    default Page<Business> searchByNameOrDescription(String query, String locationQuery, Pageable pageable) {
        Specification<Business> spec = (root, criteriaQuery, cb) -> {
            Predicate p = cb.conjunction();
            if (query != null) {
                String likePattern = "%" + query.toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), likePattern, '\\');
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), likePattern, '\\');
                p = cb.and(p, cb.or(namePredicate, descriptionPredicate));
            }
            if (locationQuery != null) {
                String likePattern = "%" + locationQuery.toLowerCase() + "%";
                Predicate cityPredicate = cb.like(cb.lower(root.get("location").get("city")), likePattern, '\\');
                Predicate countryPredicate = cb.like(cb.lower(root.get("location").get("country")), likePattern, '\\');
                Predicate namePredicate = cb.like(cb.lower(root.get("location").get("name")), likePattern, '\\');
                Predicate locationPredicate = cb.or(cityPredicate, countryPredicate, namePredicate);
                p = cb.and(p, locationPredicate);
            }
            return p;
        };
        return findAll(spec, pageable);
    }

    Page<Business> findByIndustryAndLocation_City(String industry, String city, Pageable pageable);

    Page<Business> findByIndustry(String industry, Pageable pageable);

    Page<Business> findByLocation_City(String city, Pageable pageable);

    // Extended search supporting optional filtering by associated service IDs
    default Page<Business> search(String query, String locationQuery, List<Long> serviceIds, Pageable pageable) {
        Specification<Business> spec = (root, criteriaQuery, cb) -> {
            Predicate p = cb.conjunction();

            if (query != null && !query.isBlank()) {
                String likePattern = "%" + query.toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), likePattern, '\\');
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), likePattern, '\\');
                p = cb.and(p, cb.or(namePredicate, descriptionPredicate));
            }

            if (locationQuery != null && !locationQuery.isBlank()) {
                String likePattern = "%" + locationQuery.toLowerCase() + "%";
                Predicate cityPredicate = cb.like(cb.lower(root.get("location").get("city")), likePattern, '\\');
                Predicate countryPredicate = cb.like(cb.lower(root.get("location").get("country")), likePattern, '\\');
                Predicate namePredicate = cb.like(cb.lower(root.get("location").get("name")), likePattern, '\\');
                Predicate locationPredicate = cb.or(cityPredicate, countryPredicate, namePredicate);
                p = cb.and(p, locationPredicate);
            }

            if (serviceIds != null && !serviceIds.isEmpty()) {
                Join<Business, Services> servicesJoin = root.join("services", JoinType.INNER);
                p = cb.and(p, servicesJoin.get("id").in(serviceIds));
                criteriaQuery.distinct(true); // Avoid duplicate rows due to join
            }

            return p;
        };
        return findAll(spec, pageable);
    }
}