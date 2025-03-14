package com.home.service.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.home.service.dto.SearchLogAnalyticsDTO;
import com.home.service.models.SearchLog;
import com.home.service.models.User;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
    Page<SearchLog> findByUser(User user, Pageable pageable);

    @Query("SELECT new com.home.service.dto.SearchLogAnalyticsDTO(l.query, COUNT(l)) " +
            "FROM SearchLog l WHERE l.createdAt BETWEEN :start AND :end " +
            "GROUP BY l.query ORDER BY COUNT(l) DESC")
    Page<SearchLogAnalyticsDTO> findAnalytics(LocalDateTime start, LocalDateTime end, Pageable pageable);
}