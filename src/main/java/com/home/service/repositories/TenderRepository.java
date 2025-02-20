package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.home.service.models.Tender;
import com.home.service.models.enums.TenderStatus;

import jakarta.persistence.criteria.Predicate;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public interface TenderRepository extends JpaRepository<Tender, Long>, JpaSpecificationExecutor<Tender> {
    List<Tender> findByServiceId(Long serviceId);

    List<Tender> findByStatus(TenderStatus status);

    Page<Tender> findByStatus(TenderStatus status, Pageable pageable);

    List<Tender> findByServiceIdIn(List<Long> serviceIds);

    Page<Tender> findByServiceIdIn(List<Long> serviceIds, Pageable pageable);

    // Page<Tender> findByServiceIdIn(List<Long> serviceIds, Pageable pageable, Sort
    // sort);

    Page<Tender> findByLocationAndServiceIdIn(String location, List<Long> serviceIds, Pageable pageable);

    Page<Tender> findByLocation(String location, Pageable pageable);

    List<Tender> findByDatePostedBetweenAndClosingDateBetween(Date startDatePosted, Date endDatePosted,
            Date startClosingDate, Date endClosingDate);

    default Page<Tender> advancedSearch(
            String keyword,
            TenderStatus status,
            String location,
            List<Long> serviceIds,
            LocalDateTime datePosted,
            LocalDateTime closingDate,
            Pageable pageable) {
        Specification<Tender> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (keyword != null) {
                String likePattern = "%" + keyword + "%";
                Predicate titlePredicate = cb.like(root.get("title"), likePattern, '\\');
                Predicate descriptionPredicate = cb.like(root.get("description"), likePattern, '\\');
                p = cb.and(p, cb.or(titlePredicate, descriptionPredicate));
            }
            if (status != null) {
                p = cb.and(p, cb.equal(root.get("status"), status));
            }
            if (location != null) {
                p = cb.and(p, cb.equal(root.get("location"), location));
            }
            if (serviceIds != null && !serviceIds.isEmpty()) {
                p = cb.and(p, root.get("service").get("id").in(serviceIds));
            }
            if (datePosted != null) {
                p = cb.and(p,
                        cb.equal(cb.function("DATE", Date.class, root.get("datePosted")), datePosted.toLocalDate()));
            }
            if (closingDate != null) {
                p = cb.and(p,
                        cb.equal(cb.function("DATE", Date.class, root.get("closingDate")), closingDate.toLocalDate()));
            }
            query.orderBy(cb.asc(root.get("id")));
            return p;
        };
        return findAll(spec, pageable);
    }
}