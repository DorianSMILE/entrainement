package com.ticketing.entrainement.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessExpirationMinutes;
    private final long refreshExpirationDays;

    public JwtService(
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

    public String generateAccessToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("typ", "access")
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        Instant exp = now.plus(refreshExpirationDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(username)
                .claim("typ", "refresh")
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(refreshKey)
                .compact();
    }

    public Claims parseAccessClaims(String token) {
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims parseRefreshClaims(String token) {
        return Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessExpirationMinutes() {
        return accessExpirationMinutes;
    }
}
