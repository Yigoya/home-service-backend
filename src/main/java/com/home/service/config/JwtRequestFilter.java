package com.home.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.home.service.models.CustomDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println("Authorization Header : " + authorizationHeader);
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                // Token expired: do not authenticate, continue chain for public endpoints
                username = null;
            } catch (JwtException | IllegalArgumentException e) {
                // Malformed or invalid token: ignore and proceed without authentication
                username = null;
            }
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("access_token".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }

            if (jwt != null && !jwt.isBlank()) {
                try {
                    username = jwtUtil.extractUsername(jwt);
                } catch (ExpiredJwtException e) {
                    username = null;
                } catch (JwtException | IllegalArgumentException e) {
                    username = null;
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            CustomDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            try {
                if (jwtUtil.validateToken(jwt, userDetails)) {
                System.out.println("Email : " + userDetails.toString());
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            } catch (ExpiredJwtException e) {
                // Token expired: do not set authentication
            } catch (JwtException | IllegalArgumentException e) {
                // Invalid token: ignore
            }
        }
        chain.doFilter(request, response);
    }
}
