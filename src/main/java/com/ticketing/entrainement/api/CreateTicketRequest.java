package com.ticketing.entrainement.api;

import com.ticketing.entrainement.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 10_000) String description,
        @NotNull TicketPriority priority
) {}
