package com.ticketing.entrainement.domain.exception;

public class InvalidTicketTitleException extends RuntimeException {
    public InvalidTicketTitleException(String message) {
        super(message);
    }
}
