package com.ticketing.entrainement.infrastructure.adapter.security;

import com.ticketing.entrainement.application.ports.security.TokenClaims;
import com.ticketing.entrainement.application.ports.security.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JjwtTokenProvider implements TokenProvider {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessExpirationMinutes;
    private final long refreshExpirationDays;

    public JjwtTokenProvider(
            @Value("${security.jwt.access-secret}") String accessSecret,
            @Value("${security.jwt.refresh-secret}") String refreshSecret,
            @Value("${security.jwt.access-expiration-minutes}") long accessExpirationMinutes,
            @Value("${security.jwt.refresh-expiration-days}") long refreshExpirationDays
    ) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        this.accessExpirationMinutes = accessExpirationMinutes;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    @Override
    public String generateAccessToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("typ", "access")
                .id(UUID.randomUUID().toString()) // jti standard
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(accessKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        Instant exp = now.plus(refreshExpirationDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(username)
                .claim("typ", "refresh")
                .id(UUID.randomUUID().toString()) // jti standard
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(refreshKey)
                .compact();
    }

    @Override
    public TokenClaims parseAccessClaims(String token) {
        Claims c = Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token).getPayload();
        @SuppressWarnings("unchecked")
        List<String> roles = c.get("roles", List.class);
        if (roles == null) roles = Collections.emptyList();
        return new TokenClaims(c.getSubject(), roles, String.valueOf(c.get("typ")), c.getId());
    }

    @Override
    public TokenClaims parseRefreshClaims(String token) {
        Claims c = Jwts.parser().verifyWith(refreshKey).build().parseSignedClaims(token).getPayload();
        return new TokenClaims(c.getSubject(), List.of(), String.valueOf(c.get("typ")), c.getId());
    }

    @Override
    public long getAccessExpirationMinutes() {
        return accessExpirationMinutes;
    }
}
