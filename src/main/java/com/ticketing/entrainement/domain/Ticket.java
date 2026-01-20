package com.ticketing.entrainement.domain;

import java.time.Instant;
import java.util.UUID;

public record Ticket(
    UUID id,
    String title,
    String description,
    TicketStatus status,
    TicketPriority priority,
    Instant createdAt,
    Instant updatedAt
) {}