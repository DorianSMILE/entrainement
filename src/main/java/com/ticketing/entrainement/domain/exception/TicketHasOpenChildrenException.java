package com.ticketing.entrainement.domain.exception;

import java.util.UUID;

public class TicketHasOpenChildrenException extends RuntimeException {
    public TicketHasOpenChildrenException(UUID ticketId, long remaining) {
        super("Cannot close ticket " + ticketId + ": " + remaining + " child(ren) not closed");
    }
}