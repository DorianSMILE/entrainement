package com.ticketing.entrainement.commun;

import com.ticketing.entrainement.commun.exception.DuplicateErrorResponse;
import com.ticketing.entrainement.commun.exception.NotFoundException;
import com.ticketing.entrainement.domain.exception.*;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleNotFound(NotFoundException ex) {
        return new ErrorResponse("NOT_FOUND", ex.getMessage(), Instant.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PropertyReferenceException.class)
    public ErrorResponse handleBadSort(PropertyReferenceException ex) {
        return new ErrorResponse("INVALID_SORT", ex.getMessage(), Instant.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        Throwable root = rootCause(ex);

        if (root instanceof PropertyReferenceException pre) {
            return new ErrorResponse("INVALID_SORT", pre.getMessage(), Instant.now());
        }

        return new ErrorResponse("BAD_REQUEST", ex.getMessage(), Instant.now());
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }

    @ResponseStatus(HttpStatus.CONFLICT) // 409
    @ExceptionHandler(TicketClosedException.class)
    public ErrorResponse handleTicketClosed(TicketClosedException ex) {
        return new ErrorResponse("TICKET_CLOSED", ex.getMessage(), Instant.now());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InvalidTicketStatusTransition.class)
    public ErrorResponse handleInvalidTransition(InvalidTicketStatusTransition ex) {
        return new ErrorResponse("INVALID_STATUS_TRANSITION", ex.getMessage(), Instant.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidTicketTitleException.class)
    public ErrorResponse handleInvalidTitle(InvalidTicketTitleException ex) {
        return new ErrorResponse("INVALID_TITLE", ex.getMessage(), Instant.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidTicketDescriptionException.class)
    public ErrorResponse handleInvalidDescription(InvalidTicketDescriptionException ex) {
        return new ErrorResponse("INVALID_DESCRIPTION", ex.getMessage(), Instant.now());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateTicketException.class)
    public DuplicateErrorResponse handleDuplicate(DuplicateTicketException ex) {
        return new DuplicateErrorResponse(
                "DUPLICATE_TICKET",
                ex.getMessage(),
                ex.duplicates(),
                Instant.now()
        );
    }

}