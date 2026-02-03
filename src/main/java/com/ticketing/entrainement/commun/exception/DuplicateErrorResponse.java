package com.ticketing.entrainement.commun.exception;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DuplicateErrorResponse(
        String code,
        String message,
        List<UUID> duplicates,
        Instant timestamp
) {}
