package com.ticketing.entrainement.api;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthRequest(
        @Schema(example = "admin")
        String username,
        @Schema(example = "admin")
        String password
) {}