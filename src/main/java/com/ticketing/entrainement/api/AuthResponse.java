package com.ticketing.entrainement.api;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInMinutes
) {}