package com.ticketing.entrainement.api;

import com.ticketing.entrainement.domain.TicketPriority;
import com.ticketing.entrainement.domain.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record PatchTicketRequest(
        @Schema(example = "Updated title")
        @Size(max = 200) String title,
        @Schema(example = "Updated description")
        @Size(max = 10_000) String description,
        @Schema(example = "IN_PROGRESS")
        TicketStatus status,
        @Schema(example = "HIGH")
        TicketPriority priority
) {}
