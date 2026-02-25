package com.home.service.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/auth/login",
            "/auth/admin/login",
            "/auth/password-reset-request",
            "/auth/reset-password");

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Value("${security.auth.rate-limit.max-attempts:15}")
    private int maxAttempts;

    @Value("${security.auth.rate-limit.window-seconds:60}")
    private long windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!isProtectedEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRequestURI() + "|" + resolveClientIp(request);
        LocalDateTime now = LocalDateTime.now();

        AttemptWindow window = attempts.compute(key, (k, existing) -> {
            if (existing == null || now.isAfter(existing.windowStart.plusSeconds(windowSeconds))) {
                return new AttemptWindow(now, 1);
            }
            existing.count++;
            return existing;
        });

        if (window.count > maxAttempts) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Too many authentication attempts. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isProtectedEndpoint(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod()) && PROTECTED_PATHS.contains(request.getRequestURI());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class AttemptWindow {
        private final LocalDateTime windowStart;
        private int count;

        private AttemptWindow(LocalDateTime windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
