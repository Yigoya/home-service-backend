package com.home.service.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.home.service.dto.AgencySearchCriteria;
import com.home.service.models.AgencyProfile;
import com.home.service.models.enums.VerificationStatus;

import jakarta.persistence.criteria.Predicate;

@Repository
public interface AgencyProfileRepository
        extends JpaRepository<AgencyProfile, Long>, JpaSpecificationExecutor<AgencyProfile> {
    default List<AgencyProfile> searchAgencies(AgencySearchCriteria criteria) {
        return findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getServiceType() != null) {
                predicates.add(cb.equal(root.join("services").get("name"), criteria.getServiceType()));
            }

            if (criteria.getLocation() != null) {
                predicates.add(cb.equal(root.get("city"), criteria.getLocation()));
            }

            if (criteria.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), criteria.getMinRating()));
            }

            if (criteria.getMaxRating() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), criteria.getMaxRating()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    default List<AgencyProfile> findByServiceId(Long serviceId) {
        return findAll((root, query, cb) -> {
            return cb.equal(root.join("services").get("id"), serviceId);
        });
    }

    List<AgencyProfile> findByVerificationStatus(VerificationStatus verificationStatus);
}