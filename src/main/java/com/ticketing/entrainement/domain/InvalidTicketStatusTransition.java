package com.ticketing.entrainement.domain;

public class InvalidTicketStatusTransition extends RuntimeException {
    public InvalidTicketStatusTransition(TicketStatus from, TicketStatus to) {
        super("Invalid ticket status transition: " + from + " -> " + to);
    }
}