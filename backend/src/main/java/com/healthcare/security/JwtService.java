
package com.healthcare.monitoring.security;

import com.healthcare.monitoring.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;

@Service
public class JwtService {

    @Value("${JWT_SECRET:}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    // Generate JWT signing key
    private SecretKey getSigningKey() {

        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET environment variable is not configured"
            );
        }

        return Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(jwtSecret)
        );
    }

    // Generate token after successful login
    public String generateToken(User user) {

        long currentTime = System.currentTimeMillis();

        return Jwts.builder()

            .subject(user.getEmail())

            .claim("userId", user.getId())
            .claim("role", user.getRole())

            .issuedAt(new Date(currentTime))

            .expiration(
                new Date(currentTime + jwtExpirationMs)
            )

            .signWith(getSigningKey())

            .compact();
    }

    // Extract email from JWT
    public String extractUsername(String token) {

        return extractAllClaims(token).getSubject();
    }

    // Extract user ID from JWT
    public Long extractUserId(String token) {

        Number userId = extractAllClaims(token)
            .get("userId", Number.class);

        return userId.longValue();
    }

    // Extract role from JWT
    public String extractRole(String token) {

        return extractAllClaims(token)
            .get("role", String.class);
    }

    // Validate token and match email
    public boolean isTokenValid(
        String token,
        String email
    ) {

        try {

            Claims claims = extractAllClaims(token);

            return claims.getSubject().equals(email)
                && claims.getExpiration().after(new Date());

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }

    // Verify JWT signature and extract claims
    private Claims extractAllClaims(String token) {

        return Jwts.parser()

            .verifyWith(getSigningKey())

            .build()

            .parseSignedClaims(token)

            .getPayload();
    }
}
