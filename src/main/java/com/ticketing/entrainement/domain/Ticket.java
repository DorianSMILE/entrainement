package com.ticketing.entrainement.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record Ticket(
    UUID id,
    String title,
    String description,
    TicketStatus status,
    TicketPriority priority,
    Instant createdAt,
    Instant updatedAt
) {

    public Ticket rename(String newTitle) {
        if (newTitle == null) return this;

        String t = newTitle.trim();
        if (t.isEmpty() || t.equals(title)) return this;

        return new Ticket(id, t, description, status, priority, createdAt, Instant.now());
    }

    public Ticket changeDescription(String newDescription) {
        if (newDescription == null || newDescription.equals(description)) return this;
        return new Ticket(id, title, newDescription, status, priority, createdAt, Instant.now());
    }

    public Ticket changePriority(TicketPriority newPriority) {
        if (newPriority == null || newPriority == priority) return this;
        return new Ticket(id, title, description, status, newPriority, createdAt, Instant.now());
    }

    public Ticket changeStatus(TicketStatus newStatus) {
        if (newStatus == null || newStatus == status) return this;

        if (!isAllowed(status, newStatus)) {
            throw new InvalidTicketStatusTransition(status, newStatus);
        }

        return new Ticket(id, title, description, newStatus, priority, createdAt, Instant.now());
    }

    private static boolean isAllowed(TicketStatus from, TicketStatus to) {
        return switch (from) {
            case OPEN -> Set.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED).contains(to);
            case IN_PROGRESS -> Objects.equals(TicketStatus.CLOSED, to);
            case RESOLVED, CLOSED -> false;
        };
    }
}