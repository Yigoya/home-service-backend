package com.home.service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.home.service.models.CustomDetails;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessTokenExpirationMs;

    public JwtUtil(
            @Value("${security.jwt.secret:change-this-jwt-secret-to-a-strong-32plus-char-key}") String secretKey,
            @Value("${security.jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username) {
        return generateToken(username, accessTokenExpirationMs);
    }

    public String generateToken(String username, long ttlMs) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ttlMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, CustomDetails userDetails) {
        final String username = extractUsername(token);
        System.out.println(username + " " + userDetails.getUsername());
        return (Objects.equals(username, userDetails.getUsername()) && !isTokenExpired(token));
    }
}
