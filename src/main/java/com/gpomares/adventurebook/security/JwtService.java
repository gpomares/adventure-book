package com.gpomares.adventurebook.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;
    private final Clock clock;

    @Autowired
    public JwtService(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    JwtService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.signingKey = Keys.hmacShaKeyFor(decodeSecret(properties.signingSecret()));
    }

    public String createAccessToken(Long userId, String email) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.accessTokenExpiry());
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public AuthenticatedUser validate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String email = claims.get("email", String.class);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("JWT email claim is required");
        }
        try {
            return new AuthenticatedUser(Long.valueOf(claims.getSubject()), email);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("JWT subject must be a user ID", exception);
        }
    }

    public long accessTokenExpiresInSeconds() {
        return properties.accessTokenExpiry().toSeconds();
    }

    private static byte[] decodeSecret(String encodedSecret) {
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException("JWT_SIGNING_SECRET must be configured");
        }
        try {
            return Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SIGNING_SECRET must be Base64 encoded", exception);
        }
    }
}
