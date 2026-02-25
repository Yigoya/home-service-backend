package com.home.service.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.RefreshTokenSession;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, Long> {
    Optional<RefreshTokenSession> findByTokenHash(String tokenHash);

    void deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}
