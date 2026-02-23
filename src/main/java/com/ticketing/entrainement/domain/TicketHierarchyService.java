package com.ticketing.entrainement.domain;

import java.util.UUID;
import java.util.function.Function;

// On est dans le domain, on ne met pas de @Service car on ne veut pas de dépendance avec Spring
public class TicketHierarchyService {

    public void validateNoCycle(
            UUID ticketId,
            UUID newParentId,
            Function<UUID, UUID> parentProvider
    ) {
        UUID current = newParentId;

        while (current != null) {
            if (current.equals(ticketId)) {
                throw new IllegalStateException("Cycle detected in ticket hierarchy");
            }
            current = parentProvider.apply(current);
        }
    }
}