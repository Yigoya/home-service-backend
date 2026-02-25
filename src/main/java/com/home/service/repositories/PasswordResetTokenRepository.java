package com.home.service.repositories;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.PasswordResetToken;
import com.home.service.models.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);

    PasswordResetToken findByCode(String code);

    PasswordResetToken findFirstByTokenOrderByExpiryDateDesc(String token);

    PasswordResetToken findByUser(User user);

    void deleteAllByExpiryDateBefore(LocalDateTime expiryDate);
}
