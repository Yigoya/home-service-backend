package com.home.service.services;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.home.service.config.exceptions.PasswordException;

@Service
public class PasswordPolicyService {

    private static final int MIN_LENGTH = 12;
    private static final int PASSPHRASE_MIN_LENGTH = 14;

    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "12345678", "123456789", "password", "password123", "qwerty123",
            "admin123", "welcome123", "letmein", "abcd1234", "11111111");

    public void validateOrThrow(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new PasswordException("Password is required");
        }

        String trimmed = rawPassword.trim();
        String lower = trimmed.toLowerCase();

        if (COMMON_PASSWORDS.contains(lower)) {
            throw new PasswordException("Password is too common. Please choose a stronger password or passphrase.");
        }

        boolean looksLikePassphrase = isPassphrase(trimmed);
        if (looksLikePassphrase) {
            return;
        }

        if (trimmed.length() < MIN_LENGTH
                || !UPPER.matcher(trimmed).matches()
                || !LOWER.matcher(trimmed).matches()
                || !DIGIT.matcher(trimmed).matches()
                || !SPECIAL.matcher(trimmed).matches()) {
            throw new PasswordException(
                    "Password must be at least 12 characters and include upper, lower, number, and special character, or use a passphrase with 3+ words.");
        }
    }

    private boolean isPassphrase(String password) {
        if (password.length() < PASSPHRASE_MIN_LENGTH) {
            return false;
        }
        String[] words = password.trim().split("\\s+");
        if (words.length < 3) {
            return false;
        }
        for (String word : words) {
            if (word.length() < 2) {
                return false;
            }
        }
        return true;
    }
}
