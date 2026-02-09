package com.ticketing.entrainement.api;

public record AuthResponse(String token, String tokenType, long expiresInMinutes) {}