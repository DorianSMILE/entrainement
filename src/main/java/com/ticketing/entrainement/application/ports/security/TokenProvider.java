package com.ticketing.entrainement.application.ports.security;

import java.util.List;

public interface TokenProvider {
    String generateAccessToken(String username, List<String> roles);
    String generateRefreshToken(String username);
    TokenClaims parseAccessClaims(String token);
    TokenClaims parseRefreshClaims(String token);
    long getAccessExpirationMinutes();
}