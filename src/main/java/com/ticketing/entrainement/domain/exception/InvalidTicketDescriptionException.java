package com.ticketing.entrainement.domain.exception;

public class InvalidTicketDescriptionException extends RuntimeException {
    public InvalidTicketDescriptionException(String message) {
        super(message);
    }
}
