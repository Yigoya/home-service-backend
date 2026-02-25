package com.home.service.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.models.RefreshTokenSession;
import com.home.service.models.User;
import com.home.service.repositories.RefreshTokenSessionRepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final long refreshTokenExpirationMs;

    public record RotationResult(User user, String newPlainToken) {
    }

    public RefreshTokenService(
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            @Value("${security.jwt.refresh-token-expiration-ms:1209600000}") long refreshTokenExpirationMs) {
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public String issueToken(User user) {
        String plainToken = generateSecureToken();
        RefreshTokenSession session = new RefreshTokenSession();
        session.setUser(user);
        session.setTokenHash(hashToken(plainToken));
        session.setExpiresAt(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000));
        session.setRevoked(false);
        refreshTokenSessionRepository.save(session);
        return plainToken;
    }

    @Transactional
    public RotationResult rotate(String plainToken) {
        String hash = hashToken(plainToken);
        RefreshTokenSession session = refreshTokenSessionRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalStateException("Invalid refresh token"));

        if (session.isRevoked() || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setRevoked(true);
            refreshTokenSessionRepository.save(session);
            throw new IllegalStateException("Expired or revoked refresh token");
        }

        String nextPlain = generateSecureToken();
        String nextHash = hashToken(nextPlain);

        session.setRevoked(true);
        session.setReplacedByHash(nextHash);
        refreshTokenSessionRepository.save(session);

        RefreshTokenSession next = new RefreshTokenSession();
        next.setUser(session.getUser());
        next.setTokenHash(nextHash);
        next.setExpiresAt(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000));
        next.setRevoked(false);
        refreshTokenSessionRepository.save(next);

        return new RotationResult(session.getUser(), nextPlain);
    }

    @Transactional
    public void revoke(String plainToken) {
        if (plainToken == null || plainToken.isBlank()) {
            return;
        }
        String hash = hashToken(plainToken);
        refreshTokenSessionRepository.findByTokenHash(hash).ifPresent(session -> {
            session.setRevoked(true);
            refreshTokenSessionRepository.save(session);
        });
    }

    public String hashToken(String plainToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String generateSecureToken() {
        byte[] random = new byte[64];
        new java.security.SecureRandom().nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }
}
