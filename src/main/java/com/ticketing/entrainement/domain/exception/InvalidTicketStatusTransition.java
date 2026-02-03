package com.ticketing.entrainement.domain.exception;

import com.ticketing.entrainement.domain.TicketStatus;

public class InvalidTicketStatusTransition extends RuntimeException {
    public InvalidTicketStatusTransition(TicketStatus from, TicketStatus to) {
        super("Invalid ticket status transition: " + from + " -> " + to);
    }
}