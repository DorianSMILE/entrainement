package com.ticketing.entrainement.api;

import com.ticketing.entrainement.domain.Ticket;

public record TicketDuplicateResult(
        Ticket ticket,
        MatchType matchType,
        Double score
) {
    public enum MatchType { EXACT, FUZZY }
}