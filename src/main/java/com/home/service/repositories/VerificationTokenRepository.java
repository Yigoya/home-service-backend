package com.home.service.repositories;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.PasswordResetToken;
import com.home.service.models.User;
import com.home.service.models.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);

    PasswordResetToken findByUser(User user);

    void deleteAllByExpiryDateBefore(LocalDateTime expiryDate);
}
