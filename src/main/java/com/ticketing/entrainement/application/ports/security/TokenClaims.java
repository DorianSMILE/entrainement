package com.ticketing.entrainement.application.ports.security;

import java.util.List;

public record TokenClaims(
        String subject,
        List<String> roles,
        String type,
        String jti
) {}
