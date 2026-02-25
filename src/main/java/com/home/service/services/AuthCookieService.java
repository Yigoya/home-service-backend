package com.home.service.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final boolean secureCookies;
    private final String sameSite;
    private final String domain;
    private final String path;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public AuthCookieService(
            @Value("${security.cookies.secure:true}") boolean secureCookies,
            @Value("${security.cookies.same-site:None}") String sameSite,
            @Value("${security.cookies.domain:}") String domain,
            @Value("${security.cookies.path:/}") String path,
            @Value("${security.jwt.access-token-expiration-ms:900000}") long accessTtlMs,
            @Value("${security.jwt.refresh-token-expiration-ms:1209600000}") long refreshTtlMs) {
        this.secureCookies = secureCookies;
        this.sameSite = sameSite;
        this.domain = domain;
        this.path = path;
        this.accessTtlMs = accessTtlMs;
        this.refreshTtlMs = refreshTtlMs;
    }

    public void writeAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        addCookie(response, ACCESS_TOKEN_COOKIE, accessToken, accessTtlMs);
        if (refreshToken != null && !refreshToken.isBlank()) {
            addCookie(response, REFRESH_TOKEN_COOKIE, refreshToken, refreshTtlMs);
        }
    }

    public void rotateRefreshCookie(HttpServletResponse response, String refreshToken) {
        addCookie(response, REFRESH_TOKEN_COOKIE, refreshToken, refreshTtlMs);
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, ACCESS_TOKEN_COOKIE, "", 0);
        addCookie(response, REFRESH_TOKEN_COOKIE, "", 0);
    }

    private void addCookie(HttpServletResponse response, String name, String value, long maxAgeMs) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookies)
                .path(path)
                .sameSite(sameSite)
                .maxAge(maxAgeMs / 1000);

        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
}
