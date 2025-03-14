package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.home.service.models.Business;
import com.home.service.models.User;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    @Query("SELECT b FROM Business b WHERE b.location.id = :locationId")
    Page<Business> findByLocationId(Long locationId, Pageable pageable);

    @Query("SELECT b FROM Business b WHERE b.isFeatured = true")
    Page<Business> findByIsFeaturedTrue(Pageable pageable);

    Page<Business> findByOwner(User owner, Pageable pageable);

    @Query("SELECT b FROM Business b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND (:locationId IS NULL OR b.location.id = :locationId)")
    Page<Business> searchByNameOrDescription(String query, Long locationId, Pageable pageable);
}