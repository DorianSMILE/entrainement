package com.ticketing.entrainement.domain.exception;

import java.util.List;
import java.util.UUID;

public class DuplicateTicketException extends RuntimeException {

    private final List<UUID> duplicates;

    public DuplicateTicketException(String message, List<UUID> duplicates) {
        super(message);
        this.duplicates = duplicates;
    }

    public List<UUID> duplicates() {
        return duplicates;
    }
}
