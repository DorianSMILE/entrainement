package com.ticketing.entrainement.domain.exception;

import java.util.UUID;

public class TicketClosedException extends RuntimeException {
    public TicketClosedException(UUID message) {
        super(String.valueOf(message));
    }
}
