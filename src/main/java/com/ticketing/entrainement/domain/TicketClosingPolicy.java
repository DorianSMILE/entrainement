package com.ticketing.entrainement.domain;

import com.ticketing.entrainement.domain.exception.TicketHasOpenChildrenException;

import java.util.UUID;

public class TicketClosingPolicy {

    public void ensureClosable(UUID ticketId, ChildrenCounter counter) {
        long remaining = counter.countNotClosed(ticketId);
        if (remaining > 0) {
            throw new TicketHasOpenChildrenException(ticketId, remaining);
        }
    }
}