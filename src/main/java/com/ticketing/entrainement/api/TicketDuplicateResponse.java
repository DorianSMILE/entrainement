package com.ticketing.entrainement.api;

public record TicketDuplicateResponse(
        TicketResponse ticket,
        TicketDuplicateResult.MatchType matchType,
        Double score
) {}
