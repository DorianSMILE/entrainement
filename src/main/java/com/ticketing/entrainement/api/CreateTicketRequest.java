package com.ticketing.entrainement.api;

import com.ticketing.entrainement.domain.TicketPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @Schema(example = "Cannot login to the application")
        @NotBlank @Size(max = 200)
        String title,
        @Schema(example = "User gets 500 error after entering credentials")
        @Size(max = 10_000) String
        description,
        @Schema(example = "MEDIUM")
        @NotNull
        TicketPriority priority
) {}
