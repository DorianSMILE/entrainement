package com.ticketing.entrainement.domain.exception;

import com.ticketing.entrainement.application.DuplicateCandidate;

import java.util.List;
import java.util.UUID;

public class DuplicateTicketException extends RuntimeException {

    private final List<UUID> duplicates;
    private final List<DuplicateCandidate> fuzzy;

    public DuplicateTicketException(String message, List<UUID> duplicates, List<DuplicateCandidate> fuzzy) {
        super(message);
        this.duplicates = duplicates == null ? List.of() : duplicates;
        this.fuzzy = fuzzy == null ? List.of() : fuzzy;
    }

    public List<UUID> duplicates() {
        return duplicates;
    }

    public List<DuplicateCandidate> fuzzy() {
        return fuzzy;
    }

    public boolean hasExact() {
        return !duplicates.isEmpty();
    }

    public boolean hasFuzzy() {
        return !fuzzy.isEmpty();
    }
}