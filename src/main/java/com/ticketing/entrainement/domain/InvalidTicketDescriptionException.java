package com.ticketing.entrainement.domain;

public class InvalidTicketDescriptionException extends RuntimeException {
    public InvalidTicketDescriptionException(String message) {
        super(message);
    }
}
