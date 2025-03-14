package com.home.service.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.home.service.dto.SearchLogAnalyticsDTO;
import com.home.service.models.SearchLog;
import com.home.service.models.User;
import com.home.service.models.enums.UserRole;
import com.home.service.repositories.SearchLogRepository;
import com.home.service.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;

@Service
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;
    private final UserRepository userRepository;

    public SearchLogService(SearchLogRepository searchLogRepository, UserRepository userRepository) {
        this.searchLogRepository = searchLogRepository;
        this.userRepository = userRepository;
    }

    public SearchLog getSearchLogById(Long id, Long currentUserId) {
        SearchLog log = searchLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Search log not found with ID: " + id));
        if (!userRepository.findById(currentUserId).map(User::getRole).orElse(null).equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("Only admins can view search logs");
        }
        return log;
    }

    public Page<SearchLog> getAllSearchLogs(int page, int size, Long currentUserId) {
        if (!userRepository.findById(currentUserId).map(User::getRole).orElse(null).equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("Only admins can view search logs");
        }
        Pageable pageable = PageRequest.of(page, size);
        return searchLogRepository.findAll(pageable);
    }

    public Page<SearchLog> getSearchLogsByUser(Long userId, int page, int size, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        if (!user.getId().equals(currentUserId) &&
                !userRepository.findById(currentUserId).map(User::getRole).orElse(null).equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("Only the user or an admin can view their search logs");
        }
        Pageable pageable = PageRequest.of(page, size);
        return searchLogRepository.findByUser(user, pageable);
    }

    public Page<SearchLogAnalyticsDTO> getSearchAnalytics(String startDate, String endDate, int page, int size,
            Long currentUserId) {
        if (!userRepository.findById(currentUserId).map(User::getRole).orElse(null).equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("Only admins can view search analytics");
        }
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();
        Pageable pageable = PageRequest.of(page, size);
        // Assume a custom repository method for analytics
        return searchLogRepository.findAnalytics(start, end, pageable);
    }

}