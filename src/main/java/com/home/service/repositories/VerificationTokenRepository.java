package com.home.service.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.User;
import com.home.service.models.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByCode(String code);

    Optional<VerificationToken> findByUser(User user);

    void deleteAllByExpiryDateBefore(LocalDateTime expiryDate);
}
