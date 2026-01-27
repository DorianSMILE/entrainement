package com.ticketing.entrainement.domain;

public class InvalidTicketTitleException extends RuntimeException {
    public InvalidTicketTitleException(String message) {
        super(message);
    }
}
