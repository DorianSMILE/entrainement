package com.ticketing.entrainement.api;

import com.ticketing.entrainement.domain.TicketPriority;
import com.ticketing.entrainement.domain.TicketStatus;
import jakarta.validation.constraints.Size;

public record PatchTicketRequest(
        @Size(max = 200) String title,
        @Size(max = 10_000) String description,
        TicketStatus status,
        TicketPriority priority
) {}
