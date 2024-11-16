// package com.home.service.services;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import com.home.service.repositories.PasswordResetTokenRepository;
// import com.home.service.repositories.VerificationTokenRepository;

// import java.time.LocalDateTime;

// @Service
// public class TokenCleanupService {

// @Autowired
// private PasswordResetTokenRepository passwordResetTokenRepository;

// @Autowired
// private VerificationTokenRepository verificationTokenRepository;

// @Scheduled(cron = "0 0,30 * * * ?") // This will run every 30 minutes
// public void deleteExpiredTokens() {
// LocalDateTime now = LocalDateTime.now();
// passwordResetTokenRepository.deleteAllByExpiryDateBefore(now);
// verificationTokenRepository.deleteAllByExpiryDateBefore(now);
// }
// }
